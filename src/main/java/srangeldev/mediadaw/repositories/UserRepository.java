package srangeldev.mediadaw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import srangeldev.mediadaw.models.User;

import java.util.Optional;

/**
 * Repositorio para la entidad User.
 * Proporciona acceso a datos de usuarios con métodos personalizados.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su email (usado para autenticación)
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);
}
