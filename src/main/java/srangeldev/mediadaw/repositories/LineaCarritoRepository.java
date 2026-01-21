package srangeldev.mediadaw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import srangeldev.mediadaw.models.LineaCarrito;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad LineaCarrito.
 * Gestiona las líneas individuales dentro de cada carrito.
 *
 * Similar a LineaVentaRepository pero para el carrito de compras.
 */
@Repository
public interface LineaCarritoRepository extends JpaRepository<LineaCarrito, Long> {

    /**
     * Busca una línea específica dentro de un carrito para un producto
     * Útil para verificar si un producto ya está en el carrito antes de añadir
     */
    Optional<LineaCarrito> findByCarritoIdAndProductosId(Long carritoId, Long productosId);

    /**
     * Obtiene todas las líneas de un carrito
     */
    List<LineaCarrito> findByCarritoId(Long carritoId);

    /**
     * Elimina todas las líneas de un carrito (usado al vaciar el carrito o finalizar compra)
     */
    void deleteByCarritoId(Long carritoId);
}

