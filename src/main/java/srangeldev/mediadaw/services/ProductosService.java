package srangeldev.mediadaw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srangeldev.mediadaw.exceptions.ProductNotFoundException;
import srangeldev.mediadaw.models.Productos;
import srangeldev.mediadaw.models.Categoria;
import srangeldev.mediadaw.repositories.ProductosRepository;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar productos.
 * Solo los administradores pueden crear, editar y eliminar productos.
 */
@Service
@RequiredArgsConstructor
public class ProductosService {

    private final ProductosRepository productosRepository;

    /**
     * Obtiene todos los productos no eliminados
     */
    @Transactional(readOnly = true)
    public List<Productos> getAllProducts() {
        return productosRepository.findByDeletedFalse();
    }

    /**
     * Obtiene solo los productos disponibles (con stock > 0)
     */
    @Transactional(readOnly = true)
    public List<Productos> getAvailableProducts() {
        return productosRepository.findAvailableProducts();
    }

    /**
     * Obtiene productos por categoría
     */
    @Transactional(readOnly = true)
    public List<Productos> getProductsByCategory(Categoria category) {
        return productosRepository.findByCategoryAndDeletedFalse(category);
    }

    /**
     * Busca un producto por ID
     */
    @Transactional(readOnly = true)
    public Optional<Productos> getProductById(Long id) {
        return productosRepository.findById(id)
                .filter(p -> !p.getDeleted());
    }

    /**
     * Busca productos por nombre
     */
    @Transactional(readOnly = true)
    public List<Productos> searchProducts(String nombre) {
        return productosRepository.searchByNombre(nombre);
    }

    /**
     * Crea un nuevo producto (solo ADMIN)
     */
    @Transactional
    public Productos createProduct(Productos productos) {
        return productosRepository.save(productos);
    }

    /**
     * Actualiza un producto existente (solo ADMIN)
     */
    @Transactional
    public Productos updateProduct(Long id, Productos productosData) {
        Productos productos = productosRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productos.setNombre(productosData.getNombre());
        productos.setDescripcion(productosData.getDescripcion());
        productos.setPrecio(productosData.getPrecio());
        productos.setStock(productosData.getStock());
        productos.setCategory(productosData.getCategory());

        if (productosData.getImagen() != null) {
            productos.setImagen(productosData.getImagen());
        }

        return productosRepository.save(productos);
    }

    /**
     * Elimina lógicamente un producto (solo ADMIN)
     */
    @Transactional
    public void deleteProduct(Long id) {
        Productos productos = productosRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productos.setDeleted(true);
        productosRepository.save(productos);
    }

    /**
     * Actualiza el stock de un producto (solo ADMIN)
     */
    @Transactional
    public Productos updateStock(Long id, int newStock) {
        Productos productos = productosRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productos.setStock(newStock);
        return productosRepository.save(productos);
    }

    /**
     * Obtiene productos con stock bajo (menos de un umbral)
     */
    @Transactional(readOnly = true)
    public List<Productos> getLowStockProducts(int threshold) {
        return productosRepository.findLowStockProducts(threshold);
    }

    /**
     * Verifica si un producto tiene stock suficiente
     */
    @Transactional(readOnly = true)
    public boolean checkStock(Long productId, int quantity) {
        return productosRepository.findById(productId)
                .map(p -> p.hasStock(quantity))
                .orElse(false);
    }
}
