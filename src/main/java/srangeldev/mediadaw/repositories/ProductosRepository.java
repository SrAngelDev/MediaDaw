package srangeldev.mediadaw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import srangeldev.mediadaw.models.Productos;
import srangeldev.mediadaw.models.Categoria;

import java.util.List;

/**
 * Repositorio para la entidad Productos.
 * Proporciona acceso a datos de productos con filtros y búsquedas.
 */
@Repository
public interface ProductosRepository extends JpaRepository<Productos, Long> {

    /**
     * Busca productos no eliminados (deleted = false)
     */
    List<Productos> findByDeletedFalse();

    /**
     * Busca productos por categoría que no estén eliminados
     */
    List<Productos> findByCategoryAndDeletedFalse(Categoria category);

    /**
     * Busca productos disponibles (con stock > 0 y no eliminados)
     */
    @Query("SELECT p FROM Productos p WHERE p.deleted = false AND p.stock > 0")
    List<Productos> findAvailableProducts();

    /**
     * Busca productos por nombre (búsqueda parcial, case-insensitive)
     */
    @Query("SELECT p FROM Productos p WHERE p.deleted = false AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Productos> searchByNombre(String nombre);

    /**
     * Busca productos con stock bajo (menor que el umbral especificado)
     */
    @Query("SELECT p FROM Productos p WHERE p.deleted = false AND p.stock < :threshold")
    List<Productos> findLowStockProducts(int threshold);
}
