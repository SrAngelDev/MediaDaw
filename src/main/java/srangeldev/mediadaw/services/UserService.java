package srangeldev.mediadaw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srangeldev.mediadaw.exceptions.UserNotFoundException;
import srangeldev.mediadaw.models.Role;
import srangeldev.mediadaw.models.User;
import srangeldev.mediadaw.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar usuarios.
 * Gestiona tanto clientes (USER) como administradores (ADMIN).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    // En producción, inyectar PasswordEncoder para encriptar contraseñas

    /**
     * Registra un nuevo usuario (cliente por defecto)
     */
    @Transactional
    public User registerUser(User user) {
        // Verificar que el email no esté registrado
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // TODO: Encriptar password con BCrypt en producción
        // user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Por defecto es USER
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        return userRepository.save(user);
    }

    /**
     * Busca un usuario por email (para autenticación)
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Busca un usuario por ID
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Obtiene todos los usuarios (solo ADMIN)
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Actualiza el perfil de un usuario
     */
    @Transactional
    public User updateProfile(Long userId, User userData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setNombre(userData.getNombre());
        user.setApellidos(userData.getApellidos());

        if (userData.getAvatar() != null) {
            user.setAvatar(userData.getAvatar());
        }

        return userRepository.save(user);
    }

    /**
     * Cambia el rol de un usuario (solo ADMIN)
     */
    @Transactional
    public User changeRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setRole(newRole);
        return userRepository.save(user);
    }

    /**
     * Elimina un usuario (BORRADO LÓGICO - solo ADMIN)
     * No elimina físicamente de la BD para mantener auditoría y relaciones.
     * El usuario no podrá hacer login debido a CustomUserDetailsService.
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setIsDeleted(true);
        userRepository.save(user);
    }

    /**
     * Reactiva un usuario previamente eliminado (solo ADMIN)
     */
    @Transactional
    public User restoreUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setIsDeleted(false);
        return userRepository.save(user);
    }

    /**
     * Obtiene todos los usuarios activos (no eliminados)
     */
    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.getIsDeleted())
                .toList();
    }

    /**
     * Verifica si un usuario está activo (no eliminado)
     */
    @Transactional(readOnly = true)
    public boolean isUserActive(Long userId) {
        return userRepository.findById(userId)
                .map(user -> !user.getIsDeleted())
                .orElse(false);
    }

    /**
     * Verifica si un usuario es administrador
     */
    @Transactional(readOnly = true)
    public boolean isAdmin(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getRole() == Role.ADMIN)
                .orElse(false);
    }
}
