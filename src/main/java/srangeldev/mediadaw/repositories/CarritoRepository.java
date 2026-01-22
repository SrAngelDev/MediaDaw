package srangeldev.mediadaw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import srangeldev.mediadaw.models.Carrito;
import srangeldev.mediadaw.models.User;

import java.util.Optional;

/**
 * Repositorio para la entidad Carrito.
 * Gestiona los carritos de compra activos de los usuarios.
 *
 * Relación: User 1:1 Carrito 1:N LineaCarrito
 * Similar a: User 1:N Venta 1:N LineaVenta
 */
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    /**
     * Busca el carrito de un usuario por su ID
     */
    Optional<Carrito> findByUserId(Long userId);

    /**
     * Busca el carrito de un usuario por su entidad User (CLAVE para el servicio)
     */
    Optional<Carrito> findByUser(User user);

    /**
     * Busca el carrito de un usuario con sus líneas cargadas (EAGER)
     * Útil para evitar N+1 queries al mostrar el carrito completo
     */
    @Query("SELECT c FROM Carrito c LEFT JOIN FETCH c.lineasCarrito lc LEFT JOIN FETCH lc.productos WHERE c.user.id = :userId")
    Optional<Carrito> findByUserIdWithLineas(Long userId);

    /**
     * Verifica si un usuario tiene un carrito
     */
    boolean existsByUserId(Long userId);

    /**
     * Elimina el carrito de un usuario
     */
    void deleteByUser(User user);
}
