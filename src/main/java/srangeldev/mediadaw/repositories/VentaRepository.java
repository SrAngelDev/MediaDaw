package srangeldev.mediadaw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import srangeldev.mediadaw.models.Venta;
import srangeldev.mediadaw.models.EstadoPedido;
import srangeldev.mediadaw.models.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gestiona los pedidos realizados por los usuarios.
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Busca todos los pedidos de un usuario ordenados por fecha descendente
     */
    List<Venta> findByUserOrderByFechaCompraDesc(User user);

    /**
     * Busca todos los pedidos de un usuario por ID
     */
    List<Venta> findByUserIdOrderByFechaCompraDesc(Long userId);

    /**
     * Busca pedidos por estado
     */
    List<Venta> findByEstadoOrderByFechaCompraDesc(EstadoPedido estado);

    /**
     * Busca pedidos realizados entre dos fechas
     */
    List<Venta> findByFechaCompraBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Calcula el total de ventas entre dos fechas
     */
    @Query("SELECT SUM(p.total) FROM Venta p WHERE p.fechaCompra BETWEEN :start AND :end")
    Double calculateTotalSalesBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Cuenta pedidos por estado
     */
    long countByEstado(EstadoPedido estado);
}
