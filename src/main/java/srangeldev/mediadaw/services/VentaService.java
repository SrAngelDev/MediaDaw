package srangeldev.mediadaw.services;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srangeldev.mediadaw.exceptions.PurchaseNotFoundException;
import srangeldev.mediadaw.models.*;
import srangeldev.mediadaw.repositories.CarritoRepository;
import srangeldev.mediadaw.repositories.ProductosRepository;
import srangeldev.mediadaw.repositories.VentaRepository;
import srangeldev.mediadaw.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar pedidos.
 *
 */
@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final CarritoRepository carritoRepository;
    private final ProductosRepository productosRepository;
    private final UserRepository userRepository;

    /**
     * Crea un pedido a partir del carrito de un usuario
     */
    @Transactional
    public Venta createPurchaseFromCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener el carrito con sus líneas
        Carrito carrito = carritoRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("El carrito no existe"));

        List<LineaCarrito> lineaCarritos = getLineaCarritos(carrito);

        // Paso 2: Crear el pedido
        Venta venta = Venta.builder()
                .user(user)
                .estado(EstadoPedido.PENDIENTE)
                .build();

        // Paso 3: Crear líneas de pedido y reducir stock
        for (LineaCarrito item : lineaCarritos) {
            Productos productos = item.getProductos();

            // Crear línea de pedido con el precio actual (congelado)
            LineaVenta lineaVenta = LineaVenta.builder()
                    .productos(productos)
                    .cantidad(item.getCantidad())
                    .precioVenta(productos.getPrecio()) // Precio congelado
                    .build();

            venta.addOrderLine(lineaVenta);

            // REDUCIR STOCK
            productos.reduceStock(item.getCantidad());
            productosRepository.save(productos);
        }

        // Calcular y establecer el total
        venta.setTotal(venta.calculateTotal());

        // Guardar el pedido
        Venta savedVenta = ventaRepository.save(venta);

        // Paso 4: Limpiar el carrito (eliminarlo completamente por cascade)
        carritoRepository.delete(carrito);

        return savedVenta;
    }

    private static @NonNull List<LineaCarrito> getLineaCarritos(Carrito carrito) {
        List<LineaCarrito> lineaCarritos = carrito.getLineaCarritos();

        if (lineaCarritos.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // Paso 1: Verificar stock suficiente para TODOS los productos
        for (LineaCarrito item : lineaCarritos) {
            Productos productos = item.getProductos();

            if (productos.getDeleted()) {
                throw new RuntimeException("El producto " + productos.getNombre() + " ya no está disponible");
            }

            if (!productos.hasStock(item.getCantidad())) {
                throw new RuntimeException("Stock insuficiente para " + productos.getNombre() +
                        ". Disponible: " + productos.getStock() + ", Solicitado: " + item.getCantidad());
            }
        }
        return lineaCarritos;
    }

    /**
     * Obtiene todos los pedidos de un usuario
     */
    @Transactional(readOnly = true)
    public List<Venta> getUserPurchases(Long userId) {
        return ventaRepository.findByUserIdOrderByFechaCompraDesc(userId);
    }

    /**
     * Obtiene un pedido específico
     */
    @Transactional(readOnly = true)
    public Optional<Venta> getPurchaseById(Long id) {
        return ventaRepository.findById(id);
    }

    /**
     * Actualiza el estado de un pedido (solo ADMIN)
     */
    @Transactional
    public Venta updatePurchaseStatus(Long purchaseId, EstadoPedido newStatus) {
        Venta venta = ventaRepository.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        venta.setEstado(newStatus);
        return ventaRepository.save(venta);
    }

    /**
     * Obtiene todos los pedidos por estado (solo ADMIN)
     */
    @Transactional(readOnly = true)
    public List<Venta> getPurchasesByStatus(EstadoPedido status) {
        return ventaRepository.findByEstadoOrderByFechaCompraDesc(status);
    }

    /**
     * Obtiene todos los pedidos (solo ADMIN)
     */
    @Transactional(readOnly = true)
    public List<Venta> getAllPurchases() {
        return ventaRepository.findAll();
    }

    /**
     * Cancela un pedido y restaura el stock (solo si está PENDIENTE)
     */
    @Transactional
    public void cancelPurchase(Long purchaseId) {
        Venta venta = ventaRepository.findById(purchaseId)
                .orElseThrow(() -> new PurchaseNotFoundException(purchaseId));

        if (venta.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden cancelar pedidos pendientes");
        }

        // Restaurar el stock de cada producto
        for (LineaVenta line : venta.getLines()) {
            Productos productos = line.getProductos();
            productos.increaseStock(line.getCantidad());
            productosRepository.save(productos);
        }

        // Eliminar el pedido
        ventaRepository.delete(venta);
    }

    /**
     * Calcula el total de ventas (solo ADMIN)
     */
    @Transactional(readOnly = true)
    public Double calculateTotalRevenue() {
        return ventaRepository.findAll().stream()
                .mapToDouble(Venta::getTotal)
                .sum();
    }

    /**
     * Cuenta pedidos por estado (solo ADMIN)
     */
    @Transactional(readOnly = true)
    public long countByStatus(EstadoPedido status) {
        return ventaRepository.countByEstado(status);
    }
}
