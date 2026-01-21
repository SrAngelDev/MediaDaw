package srangeldev.mediadaw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srangeldev.mediadaw.exceptions.InsufficientStockException;
import srangeldev.mediadaw.exceptions.ProductNotFoundException;
import srangeldev.mediadaw.exceptions.UserNotFoundException;
import srangeldev.mediadaw.models.Carrito;
import srangeldev.mediadaw.models.LineaCarrito;
import srangeldev.mediadaw.models.Productos;
import srangeldev.mediadaw.models.User;
import srangeldev.mediadaw.repositories.CarritoRepository;
import srangeldev.mediadaw.repositories.LineaCarritoRepository;
import srangeldev.mediadaw.repositories.ProductosRepository;
import srangeldev.mediadaw.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio para gestionar el carrito de compras.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final LineaCarritoRepository lineaCarritoRepository;
    private final ProductosRepository productosRepository;
    private final UserRepository userRepository;

    /**
     * Obtiene el carrito activo del usuario.
     * Si no existe, crea uno nuevo y lo asocia.
     *
     * Esto garantiza que nunca tendremos NullPointerException al acceder al carrito.
     */
    @Transactional(readOnly = true)
    public Carrito getCart(User user) {
        return carritoRepository.findByUser(user)
                .orElseGet(() -> createEmptyCart(user));
    }

    /**
     * Crea un carrito vacío para un usuario nuevo.
     * Este método es privado porque siempre se invoca desde getCart().
     */
    private Carrito createEmptyCart(User user) {
        Carrito cart = Carrito.builder()
                .user(user)
                .updatedAt(LocalDateTime.now())
                .build();
        return carritoRepository.save(cart);
    }

    /**
     * Añade un producto al carrito gestionando el STOCK y cantidades.
     *
     * LÓGICA:
     * 1. Obtener o crear el carrito del usuario
     * 2. Verificar si el producto ya está en el carrito
     * 3. Si existe: incrementar cantidad (validando stock)
     * 4. Si no existe: crear nueva línea (validando stock)
     * 5. Actualizar updatedAt del carrito
     *
     * @param user Usuario que añade el producto
     * @param productId ID del producto a añadir
     * @param quantity Cantidad a añadir
     */
    public void addToCart(User user, Long productId, Integer quantity) {
        // Obtener o crear carrito
        Carrito cart = getCart(user);

        // Buscar el producto
        Productos product = productosRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Verificar que el producto no esté eliminado
        if (product.getDeleted()) {
            throw new ProductNotFoundException("El producto '" + product.getNombre() + "' no está disponible");
        }

        // 1. Verificar si el producto ya está en el carrito
        Optional<LineaCarrito> existingItem = cart.getLineaCarritos().stream()
                .filter(item -> item.getProductos().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Si ya existe, sumamos la cantidad
            LineaCarrito item = existingItem.get();
            int newQuantity = item.getCantidad() + quantity;

            // VALIDACIÓN DE STOCK
            if (newQuantity > product.getStock()) {
                throw new InsufficientStockException(
                        product.getNombre(),
                        product.getStock()
                );
            }

            item.setCantidad(newQuantity);

            lineaCarritoRepository.save(item);

        } else {
            // Si es nuevo, verificamos stock inicial
            if (quantity > product.getStock()) {
                throw new InsufficientStockException(
                        product.getNombre(),
                        product.getStock()
                );
            }

            LineaCarrito newItem = LineaCarrito.builder()
                    .carrito(cart)
                    .productos(product)
                    .cantidad(quantity)
                    .build();

            // Usamos el método helper del Carrito para sincronizar la relación
            cart.addLineaCarrito(newItem);
            lineaCarritoRepository.save(newItem);
        }

        // Actualizamos la fecha de modificación del carrito
        cart.setUpdatedAt(LocalDateTime.now());
        carritoRepository.save(cart);
    }

    /**
     * Elimina un item específico del carrito.
     *
     * @param user Usuario propietario del carrito
     * @param productId ID del producto a eliminar
     */
    public void removeFromCart(User user, Long productId) {
        Carrito cart = getCart(user);

        LineaCarrito itemToRemove = cart.getLineaCarritos().stream()
                .filter(item -> item.getProductos().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("El producto no está en el carrito"));

        // Usamos el helper del modelo para desvincular correctamente
        cart.removeLineaCarrito(itemToRemove);
        lineaCarritoRepository.delete(itemToRemove); // Borrado físico de la línea

        cart.setUpdatedAt(LocalDateTime.now());
        carritoRepository.save(cart);
    }

    /**
     * Vacía el carrito por completo.
     *
     * @param user Usuario propietario del carrito
     */
    public void clearCart(User user) {
        Carrito cart = getCart(user);

        // Borramos todos los items de la base de datos
        lineaCarritoRepository.deleteAll(cart.getLineaCarritos());

        // Limpiamos la lista en memoria
        cart.getLineaCarritos().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        carritoRepository.save(cart);
    }

    /**
     * Actualiza la cantidad exacta de un item.
     * Si la cantidad es <= 0, elimina el item.
     *
     * @param user Usuario propietario del carrito
     * @param productId ID del producto a actualizar
     * @param quantity Nueva cantidad deseada
     */
    public void updateQuantity(User user, Long productId, Integer quantity) {
        if (quantity <= 0) {
            removeFromCart(user, productId);
            return;
        }

        Carrito cart = getCart(user);
        LineaCarrito item = cart.getLineaCarritos().stream()
                .filter(i -> i.getProductos().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Producto no en carrito"));

        Productos product = item.getProductos();

        // VALIDACIÓN DE STOCK
        if (quantity > product.getStock()) {
            throw new InsufficientStockException(
                    product.getNombre(),
                    product.getStock()
            );
        }

        item.setCantidad(quantity);
        lineaCarritoRepository.save(item);

        cart.setUpdatedAt(LocalDateTime.now());
        carritoRepository.save(cart);
    }

    /**
     * Calcula el total del carrito de un usuario.
     *
     * @param userId ID del usuario
     * @return Total del carrito
     */
    @Transactional(readOnly = true)
    public Double getCartTotal(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return carritoRepository.findByUser(user)
                .map(Carrito::getTotal)
                .orElse(0.0);
    }

    /**
     * Cuenta el número total de items en el carrito (suma de cantidades).
     *
     * @param userId ID del usuario
     * @return Cantidad total de items
     */
    @Transactional(readOnly = true)
    public long getCartItemCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return carritoRepository.findByUser(user)
                .map(Carrito::getTotalItems)
                .orElse(0);
    }

    /**
     * Obtiene el carrito de un usuario por su ID.
     * Útil para controladores que trabajan con userId directamente.
     *
     * @param userId ID del usuario
     * @return Carrito del usuario
     */
    @Transactional(readOnly = true)
    public Carrito getCartByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return getCart(user);
    }

    /**
     * Verifica si el carrito de un usuario está vacío.
     *
     * @param userId ID del usuario
     * @return true si el carrito está vacío, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean isCartEmpty(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return carritoRepository.findByUser(user)
                .map(c -> c.getLineaCarritos().isEmpty())
                .orElse(true);
    }
}
