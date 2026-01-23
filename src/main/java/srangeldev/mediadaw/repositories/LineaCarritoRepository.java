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

}

