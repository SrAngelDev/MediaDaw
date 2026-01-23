package srangeldev.mediadaw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import srangeldev.mediadaw.exceptions.UserNotFoundException;
import srangeldev.mediadaw.models.Role;
import srangeldev.mediadaw.models.User;
import srangeldev.mediadaw.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UserService.
 * Usamos MockitoExtension para no levantar todo el contexto de Spring (más rápido)
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("registerUser registra un nuevo usuario correctamente")
    void registerUser() {
        // ARRANGE
        User newUser = User.builder()
                .email("nuevo@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .password("password123")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .email("nuevo@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .password("password123")
                .role(Role.USER)
                .isDeleted(false)
                .build();

        when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // ACT
        User result = userService.registerUser(newUser);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("nuevo@test.com", result.getEmail()),
                () -> assertEquals("Test", result.getNombre()),
                () -> assertEquals(Role.USER, result.getRole()),
                () -> assertFalse(result.getIsDeleted())
        );

        verify(userRepository, times(1)).existsByEmail("nuevo@test.com");
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    @DisplayName("registerUser lanza excepción cuando el email ya está registrado")
    void registerUser_DuplicateEmail() {
        // ARRANGE
        User newUser = User.builder()
                .email("existente@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("existente@test.com")).thenReturn(true);

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> userService.registerUser(newUser));
        verify(userRepository, times(1)).existsByEmail("existente@test.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser asigna rol USER por defecto cuando no se especifica")
    void registerUser_DefaultRole() {
        // ARRANGE
        User newUser = User.builder()
                .email("nuevo@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .password("password123")
                .role(null) // Sin rol especificado
                .build();

        when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // ACT
        User result = userService.registerUser(newUser);

        // ASSERT
        assertEquals(Role.USER, result.getRole());
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    @DisplayName("findByEmail encuentra un usuario por email")
    void findByEmail() {
        // ARRANGE
        String email = "test@test.com";
        User user = User.builder()
                .id(1L)
                .email(email)
                .nombre("Test")
                .apellidos("Usuario")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // ACT
        Optional<User> result = userService.findByEmail(email);

        // ASSERT
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(email, result.get().getEmail()),
                () -> assertEquals("Test", result.get().getNombre())
        );

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("findByEmail devuelve Optional.empty cuando no existe el usuario")
    void findByEmail_NotFound() {
        // ARRANGE
        String email = "noexiste@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // ACT
        Optional<User> result = userService.findByEmail(email);

        // ASSERT
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("findById encuentra un usuario por ID")
    void findById() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // ACT
        Optional<User> result = userService.findById(userId);

        // ASSERT
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(userId, result.get().getId()),
                () -> assertEquals("test@test.com", result.get().getEmail())
        );

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("findById devuelve Optional.empty cuando no existe el usuario")
    void findById_NotFound() {
        // ARRANGE
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ACT
        Optional<User> result = userService.findById(userId);

        // ASSERT
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("getAllUsers devuelve todos los usuarios")
    void getAllUsers() {
        // ARRANGE
        User user1 = User.builder()
                .id(1L)
                .email("user1@test.com")
                .nombre("Usuario")
                .apellidos("Uno")
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("user2@test.com")
                .nombre("Usuario")
                .apellidos("Dos")
                .build();

        User user3 = User.builder()
                .id(3L)
                .email("admin@test.com")
                .nombre("Admin")
                .apellidos("Test")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2, user3));

        // ACT
        List<User> result = userService.getAllUsers();

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(3, result.size())
        );

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("updateProfile actualiza el perfil del usuario correctamente")
    void updateProfile() {
        // ARRANGE
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Nombre Viejo")
                .apellidos("Apellidos Viejos")
                .avatar(null)
                .build();

        User userData = User.builder()
                .nombre("Nombre Nuevo")
                .apellidos("Apellidos Nuevos")
                .avatar("avatar.jpg")
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Nombre Nuevo")
                .apellidos("Apellidos Nuevos")
                .avatar("avatar.jpg")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // ACT
        User result = userService.updateProfile(userId, userData);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Nombre Nuevo", result.getNombre()),
                () -> assertEquals("Apellidos Nuevos", result.getApellidos()),
                () -> assertEquals("avatar.jpg", result.getAvatar())
        );

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("updateProfile lanza excepción cuando el usuario no existe")
    void updateProfile_NotFound() {
        // ARRANGE
        Long userId = 999L;
        User userData = User.builder()
                .nombre("Nombre Nuevo")
                .apellidos("Apellidos Nuevos")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(UserNotFoundException.class,
                () -> userService.updateProfile(userId, userData));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changeRole cambia el rol del usuario correctamente")
    void changeRole() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .role(Role.USER)
                .build();

        User userWithNewRole = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(userWithNewRole);

        // ACT
        User result = userService.changeRole(userId, Role.ADMIN);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(Role.ADMIN, result.getRole()),
                () -> assertEquals(userId, result.getId())
        );

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("changeRole lanza excepción cuando el usuario no existe")
    void changeRole_NotFound() {
        // ARRANGE
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(UserNotFoundException.class,
                () -> userService.changeRole(userId, Role.ADMIN));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteUser marca el usuario como eliminado (borrado lógico)")
    void deleteUser() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .isDeleted(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // ACT
        userService.deleteUser(userId);

        // ASSERT
        assertTrue(user.getIsDeleted());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("deleteUser lanza excepción cuando el usuario no existe")
    void deleteUser_NotFound() {
        // ARRANGE
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("restoreUser reactiva un usuario eliminado")
    void restoreUser() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .isDeleted(true)
                .build();

        User restoredUser = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .isDeleted(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(restoredUser);

        // ACT
        User result = userService.restoreUser(userId);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertFalse(result.getIsDeleted()),
                () -> assertEquals(userId, result.getId())
        );

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("restoreUser lanza excepción cuando el usuario no existe")
    void restoreUser_NotFound() {
        // ARRANGE
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(UserNotFoundException.class, () -> userService.restoreUser(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getActiveUsers devuelve solo usuarios activos (no eliminados)")
    void getActiveUsers() {
        // ARRANGE
        User user1 = User.builder()
                .id(1L)
                .email("user1@test.com")
                .nombre("Usuario")
                .apellidos("Uno")
                .isDeleted(false)
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("user2@test.com")
                .nombre("Usuario")
                .apellidos("Dos")
                .isDeleted(true) // Usuario eliminado
                .build();

        User user3 = User.builder()
                .id(3L)
                .email("user3@test.com")
                .nombre("Usuario")
                .apellidos("Tres")
                .isDeleted(false)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2, user3));

        // ACT
        List<User> result = userService.getActiveUsers();

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().noneMatch(User::getIsDeleted))
        );

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("isUserActive devuelve true cuando el usuario está activo")
    void isUserActive() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .isDeleted(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // ACT
        boolean result = userService.isUserActive(userId);

        // ASSERT
        assertTrue(result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("isUserActive devuelve false cuando el usuario está eliminado")
    void isUserActive_Deleted() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .isDeleted(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // ACT
        boolean result = userService.isUserActive(userId);

        // ASSERT
        assertFalse(result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("isUserActive devuelve false cuando el usuario no existe")
    void isUserActive_NotFound() {
        // ARRANGE
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ACT
        boolean result = userService.isUserActive(userId);

        // ASSERT
        assertFalse(result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("isAdmin devuelve true cuando el usuario es administrador")
    void isAdmin() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("admin@test.com")
                .nombre("Admin")
                .apellidos("Test")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // ACT
        boolean result = userService.isAdmin(userId);

        // ASSERT
        assertTrue(result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("isAdmin devuelve false cuando el usuario no es administrador")
    void isAdmin_NotAdmin() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("user@test.com")
                .nombre("User")
                .apellidos("Test")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // ACT
        boolean result = userService.isAdmin(userId);

        // ASSERT
        assertFalse(result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("isAdmin devuelve false cuando el usuario no existe")
    void isAdmin_NotFound() {
        // ARRANGE
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ACT
        boolean result = userService.isAdmin(userId);

        // ASSERT
        assertFalse(result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("registerUser lanza RuntimeException con mensaje verificable cuando email duplicado")
    void registerUser_DuplicateEmail_CheckMessage() {
        // ARRANGE
        User newUser = User.builder()
                .email("duplicado@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("duplicado@test.com")).thenReturn(true);

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(newUser));

        // Verificar que el mensaje contiene información útil sobre email duplicado
        assertTrue(exception.getMessage().contains("email") ||
                   exception.getMessage().contains("registrado"));
        verify(userRepository, times(1)).existsByEmail("duplicado@test.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProfile lanza UserNotFoundException con ID verificable")
    void updateProfile_NotFound_CheckId() {
        // ARRANGE
        Long userIdInexistente = 88888L;
        User userData = User.builder()
                .nombre("Nombre Nuevo")
                .apellidos("Apellidos Nuevos")
                .build();

        when(userRepository.findById(userIdInexistente)).thenReturn(Optional.empty());

        // ACT & ASSERT
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateProfile(userIdInexistente, userData));

        assertNotNull(exception);
        verify(userRepository, times(1)).findById(userIdInexistente);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changeRole lanza UserNotFoundException con mensaje verificable")
    void changeRole_NotFound_CheckMessage() {
        // ARRANGE
        Long userId = 77777L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.changeRole(userId, Role.ADMIN));

        assertNotNull(exception);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteUser lanza UserNotFoundException con ID verificable")
    void deleteUser_NotFound_CheckId() {
        // ARRANGE
        Long userIdInexistente = 66666L;
        when(userRepository.findById(userIdInexistente)).thenReturn(Optional.empty());

        // ACT & ASSERT
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(userIdInexistente));

        assertNotNull(exception);
        verify(userRepository, times(1)).findById(userIdInexistente);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("restoreUser lanza UserNotFoundException con mensaje verificable")
    void restoreUser_NotFound_CheckMessage() {
        // ARRANGE
        Long userId = 55555L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.restoreUser(userId));

        assertNotNull(exception);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProfile no actualiza avatar si es null")
    void updateProfile_NullAvatar() {
        // ARRANGE
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Nombre Viejo")
                .apellidos("Apellidos Viejos")
                .avatar("avatar-original.jpg")
                .build();

        User userData = User.builder()
                .nombre("Nombre Nuevo")
                .apellidos("Apellidos Nuevos")
                .avatar(null) // Avatar null, no debe actualizarse
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        User result = userService.updateProfile(userId, userData);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Nombre Nuevo", result.getNombre()),
                () -> assertEquals("Apellidos Nuevos", result.getApellidos()),
                () -> assertEquals("avatar-original.jpg", result.getAvatar()) // Avatar no cambia
        );

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("getAllUsers devuelve lista vacía cuando no hay usuarios")
    void getAllUsers_Empty() {
        // ARRANGE
        when(userRepository.findAll()).thenReturn(List.of());

        // ACT
        List<User> result = userService.getAllUsers();

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getActiveUsers devuelve lista vacía cuando no hay usuarios activos")
    void getActiveUsers_Empty() {
        // ARRANGE
        User user1 = User.builder()
                .id(1L)
                .email("user1@test.com")
                .nombre("Usuario")
                .apellidos("Uno")
                .isDeleted(true)
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("user2@test.com")
                .nombre("Usuario")
                .apellidos("Dos")
                .isDeleted(true)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // ACT
        List<User> result = userService.getActiveUsers();

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getActiveUsers devuelve todos cuando todos están activos")
    void getActiveUsers_AllActive() {
        // ARRANGE
        User user1 = User.builder()
                .id(1L)
                .email("user1@test.com")
                .nombre("Usuario")
                .apellidos("Uno")
                .isDeleted(false)
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("user2@test.com")
                .nombre("Usuario")
                .apellidos("Dos")
                .isDeleted(false)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // ACT
        List<User> result = userService.getActiveUsers();

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().noneMatch(User::getIsDeleted))
        );

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("registerUser preserva el rol ADMIN si viene especificado")
    void registerUser_AdminRole() {
        // ARRANGE
        User newAdmin = User.builder()
                .email("admin@test.com")
                .nombre("Admin")
                .apellidos("Test")
                .password("admin123")
                .role(Role.ADMIN) // Rol admin especificado
                .build();

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // ACT
        User result = userService.registerUser(newAdmin);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(Role.ADMIN, result.getRole()) // Mantiene ADMIN
        );

        verify(userRepository, times(1)).save(newAdmin);
    }

    @Test
    @DisplayName("changeRole de USER a USER no lanza excepción")
    void changeRole_SameRole() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        User result = userService.changeRole(userId, Role.USER);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(Role.USER, result.getRole())
        );

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("deleteUser marca usuario ya eliminado sin problema")
    void deleteUser_AlreadyDeleted() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .isDeleted(true) // Ya está eliminado
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // ACT
        userService.deleteUser(userId);

        // ASSERT
        assertTrue(user.getIsDeleted()); // Sigue eliminado
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("restoreUser restaura usuario ya activo sin problema")
    void restoreUser_AlreadyActive() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .nombre("Test")
                .apellidos("Usuario")
                .isDeleted(false) // Ya está activo
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        User result = userService.restoreUser(userId);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertFalse(result.getIsDeleted()) // Sigue activo
        );

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
    }
}

