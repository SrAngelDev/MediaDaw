package srangeldev.mediadaw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import srangeldev.mediadaw.models.LineaVenta;

/**
 * Repositorio para la entidad LineaVenta.
 * Gestiona las líneas de pedido.
 */
@Repository
public interface LineaVentaRepository extends JpaRepository<LineaVenta, Long> {
}
