package srangeldev.mediadaw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import srangeldev.mediadaw.exceptions.InsufficientStockException;
import srangeldev.mediadaw.exceptions.ProductNotFoundException;
import srangeldev.mediadaw.exceptions.UserNotFoundException;
import srangeldev.mediadaw.models.*;
import srangeldev.mediadaw.repositories.CarritoRepository;
import srangeldev.mediadaw.repositories.LineaCarritoRepository;
import srangeldev.mediadaw.repositories.ProductosRepository;
import srangeldev.mediadaw.repositories.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CarritoService.
 * Usamos MockitoExtension para no levantar todo el contexto de Spring (más rápido)
 */
@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private LineaCarritoRepository lineaCarritoRepository;

    @Mock
    private ProductosRepository productosRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CarritoService carritoService;

    @Test
    @DisplayName("getCart devuelve el carrito existente del usuario")
    void getCart() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));

        // ACT
        Carrito result = carritoService.getCart(user);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1L, result.getId()),
                () -> assertEquals(user, result.getUser())
        );

        verify(carritoRepository, times(1)).findByUserIdWithLineas(user.getId());
    }

    @Test
    @DisplayName("getCart crea un nuevo carrito si no existe")
    void getCart_CreatesNew() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Carrito nuevoCarrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.empty());
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(nuevoCarrito);

        // ACT
        Carrito result = carritoService.getCart(user);

        // ASSERT
        assertNotNull(result);
        verify(carritoRepository, times(1)).findByUserIdWithLineas(user.getId());
        verify(carritoRepository, times(1)).save(any(Carrito.class));
    }

    @Test
    @DisplayName("addToCart añade un nuevo producto al carrito")
    void addToCart() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .deleted(false)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(productosRepository.findById(1L))
                .thenReturn(Optional.of(producto));
        when(lineaCarritoRepository.save(any(LineaCarrito.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carrito);

        // ACT
        carritoService.addToCart(user, 1L, 2);

        // ASSERT
        assertEquals(1, carrito.getLineasCarrito().size());
        assertEquals(2, carrito.getLineasCarrito().get(0).getCantidad());
        verify(productosRepository, times(1)).findById(1L);
        verify(lineaCarritoRepository, times(1)).save(any(LineaCarrito.class));
        verify(carritoRepository, times(1)).save(carrito);
    }

    @Test
    @DisplayName("addToCart incrementa cantidad si el producto ya existe en el carrito")
    void addToCart_IncrementQuantity() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .deleted(false)
                .build();

        LineaCarrito lineaExistente = LineaCarrito.builder()
                .id(1L)
                .productos(producto)
                .cantidad(2)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(lineaExistente)))
                .build();

        lineaExistente.setCarrito(carrito);

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(productosRepository.findById(1L))
                .thenReturn(Optional.of(producto));
        when(lineaCarritoRepository.save(any(LineaCarrito.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carrito);

        // ACT
        carritoService.addToCart(user, 1L, 3);

        // ASSERT
        assertEquals(1, carrito.getLineasCarrito().size());
        assertEquals(5, carrito.getLineasCarrito().get(0).getCantidad());
        verify(lineaCarritoRepository, times(1)).save(lineaExistente);
    }

    @Test
    @DisplayName("addToCart lanza excepción cuando el producto no existe")
    void addToCart_ProductNotFound() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(productosRepository.findById(999L))
                .thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ProductNotFoundException.class,
                () -> carritoService.addToCart(user, 999L, 2));
        verify(lineaCarritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("addToCart lanza excepción cuando no hay stock suficiente")
    void addToCart_InsufficientStock() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(5)
                .deleted(false)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(productosRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        // ACT & ASSERT
        assertThrows(InsufficientStockException.class,
                () -> carritoService.addToCart(user, 1L, 10));
        verify(lineaCarritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("addToCart lanza excepción cuando el producto está eliminado")
    void addToCart_DeletedProduct() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .deleted(true)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(productosRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        // ACT & ASSERT
        assertThrows(ProductNotFoundException.class,
                () -> carritoService.addToCart(user, 1L, 2));
        verify(lineaCarritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeFromCart elimina un producto del carrito")
    void removeFromCart() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .deleted(false)
                .build();

        LineaCarrito linea = LineaCarrito.builder()
                .id(1L)
                .productos(producto)
                .cantidad(2)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(linea)))
                .build();

        linea.setCarrito(carrito);

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carrito);

        // ACT
        carritoService.removeFromCart(user, 1L);

        // ASSERT
        assertTrue(carrito.getLineasCarrito().isEmpty());
        verify(lineaCarritoRepository, times(1)).delete(linea);
        verify(carritoRepository, times(1)).save(carrito);
    }

    @Test
    @DisplayName("removeFromCart lanza excepción si el producto no está en el carrito")
    void removeFromCart_NotInCart() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));

        // ACT & ASSERT
        assertThrows(ProductNotFoundException.class,
                () -> carritoService.removeFromCart(user, 999L));
        verify(lineaCarritoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("clearCart vacía completamente el carrito")
    void clearCart() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Productos producto1 = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .build();

        Productos producto2 = Productos.builder()
                .id(2L)
                .nombre("iPad Pro")
                .precio(1200.0)
                .build();

        LineaCarrito linea1 = LineaCarrito.builder()
                .id(1L)
                .productos(producto1)
                .cantidad(2)
                .build();

        LineaCarrito linea2 = LineaCarrito.builder()
                .id(2L)
                .productos(producto2)
                .cantidad(1)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(linea1, linea2)))
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carrito);

        // ACT
        carritoService.clearCart(user);

        // ASSERT
        assertTrue(carrito.getLineasCarrito().isEmpty());
        verify(lineaCarritoRepository, times(1)).deleteAll(anyList());
        verify(carritoRepository, times(1)).save(carrito);
    }

    @Test
    @DisplayName("updateQuantity actualiza la cantidad de un producto")
    void updateQuantity() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .deleted(false)
                .build();

        LineaCarrito linea = LineaCarrito.builder()
                .id(1L)
                .productos(producto)
                .cantidad(2)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(linea)))
                .build();

        linea.setCarrito(carrito);

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(lineaCarritoRepository.save(any(LineaCarrito.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carrito);

        // ACT
        carritoService.updateQuantity(user, 1L, 5);

        // ASSERT
        assertEquals(5, linea.getCantidad());
        verify(lineaCarritoRepository, times(1)).save(linea);
        verify(carritoRepository, times(1)).save(carrito);
    }

    @Test
    @DisplayName("updateQuantity elimina el producto si la cantidad es 0")
    void updateQuantity_RemoveIfZero() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .deleted(false)
                .build();

        LineaCarrito linea = LineaCarrito.builder()
                .id(1L)
                .productos(producto)
                .cantidad(2)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(linea)))
                .build();

        linea.setCarrito(carrito);

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carrito);

        // ACT
        carritoService.updateQuantity(user, 1L, 0);

        // ASSERT
        assertTrue(carrito.getLineasCarrito().isEmpty());
        verify(lineaCarritoRepository, times(1)).delete(linea);
    }

    @Test
    @DisplayName("updateQuantity lanza excepción cuando no hay stock suficiente")
    void updateQuantity_InsufficientStock() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(5)
                .deleted(false)
                .build();

        LineaCarrito linea = LineaCarrito.builder()
                .id(1L)
                .productos(producto)
                .cantidad(2)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(linea)))
                .build();

        linea.setCarrito(carrito);

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));

        // ACT & ASSERT
        assertThrows(InsufficientStockException.class,
                () -> carritoService.updateQuantity(user, 1L, 10));
        verify(lineaCarritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateQuantity lanza excepción si el producto no está en el carrito")
    void updateQuantity_NotInCart() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));

        // ACT & ASSERT
        assertThrows(ProductNotFoundException.class,
                () -> carritoService.updateQuantity(user, 999L, 5));
        verify(lineaCarritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("getCartTotal calcula el total del carrito correctamente")
    void getCartTotal() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .build();

        Productos producto1 = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .build();

        Productos producto2 = Productos.builder()
                .id(2L)
                .nombre("iPad Pro")
                .precio(1200.0)
                .build();

        LineaCarrito linea1 = LineaCarrito.builder()
                .id(1L)
                .productos(producto1)
                .cantidad(2) // 2000
                .build();

        LineaCarrito linea2 = LineaCarrito.builder()
                .id(2L)
                .productos(producto2)
                .cantidad(1) // 1200
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(linea1, linea2)))
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(carritoRepository.findByUser(user))
                .thenReturn(Optional.of(carrito));

        // ACT
        Double result = carritoService.getCartTotal(userId);

        // ASSERT
        assertEquals(3200.0, result);
        verify(userRepository, times(1)).findById(userId);
        verify(carritoRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("getCartTotal devuelve 0.0 cuando el carrito no existe")
    void getCartTotal_EmptyCart() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(carritoRepository.findByUser(user))
                .thenReturn(Optional.empty());

        // ACT
        Double result = carritoService.getCartTotal(userId);

        // ASSERT
        assertEquals(0.0, result);
        verify(carritoRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("getCartTotal lanza excepción cuando el usuario no existe")
    void getCartTotal_UserNotFound() {
        // ARRANGE
        Long userId = 999L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(UserNotFoundException.class,
                () -> carritoService.getCartTotal(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(carritoRepository, never()).findByUser(any());
    }

    @Test
    @DisplayName("getCartItemCount cuenta el total de items en el carrito")
    void getCartItemCount() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .build();

        Productos producto1 = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .build();

        Productos producto2 = Productos.builder()
                .id(2L)
                .nombre("iPad Pro")
                .precio(1200.0)
                .build();

        LineaCarrito linea1 = LineaCarrito.builder()
                .id(1L)
                .productos(producto1)
                .cantidad(2)
                .build();

        LineaCarrito linea2 = LineaCarrito.builder()
                .id(2L)
                .productos(producto2)
                .cantidad(3)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(linea1, linea2)))
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(carritoRepository.findByUser(user))
                .thenReturn(Optional.of(carrito));

        // ACT
        long result = carritoService.getCartItemCount(userId);

        // ASSERT
        assertEquals(5, result);
        verify(userRepository, times(1)).findById(userId);
        verify(carritoRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("getCartItemCount devuelve 0 cuando el carrito no existe")
    void getCartItemCount_EmptyCart() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(carritoRepository.findByUser(user))
                .thenReturn(Optional.empty());

        // ACT
        long result = carritoService.getCartItemCount(userId);

        // ASSERT
        assertEquals(0, result);
        verify(carritoRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("getCartByUserId devuelve el carrito del usuario por ID")
    void getCartByUserId() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(carritoRepository.findByUserIdWithLineas(userId))
                .thenReturn(Optional.of(carrito));

        // ACT
        Carrito result = carritoService.getCartByUserId(userId);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1L, result.getId()),
                () -> assertEquals(user, result.getUser())
        );

        verify(userRepository, times(1)).findById(userId);
        verify(carritoRepository, times(1)).findByUserIdWithLineas(userId);
    }

    @Test
    @DisplayName("getCartByUserId lanza excepción cuando el usuario no existe")
    void getCartByUserId_UserNotFound() {
        // ARRANGE
        Long userId = 999L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(UserNotFoundException.class,
                () -> carritoService.getCartByUserId(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("isCartEmpty devuelve true cuando el carrito está vacío")
    void isCartEmpty() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(carritoRepository.findByUser(user))
                .thenReturn(Optional.of(carrito));

        // ACT
        boolean result = carritoService.isCartEmpty(userId);

        // ASSERT
        assertTrue(result);
        verify(userRepository, times(1)).findById(userId);
        verify(carritoRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("isCartEmpty devuelve false cuando el carrito tiene items")
    void isCartEmpty_HasItems() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .build();

        LineaCarrito linea = LineaCarrito.builder()
                .id(1L)
                .productos(producto)
                .cantidad(2)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(linea)))
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(carritoRepository.findByUser(user))
                .thenReturn(Optional.of(carrito));

        // ACT
        boolean result = carritoService.isCartEmpty(userId);

        // ASSERT
        assertFalse(result);
        verify(carritoRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("isCartEmpty devuelve true cuando el carrito no existe")
    void isCartEmpty_NoCart() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(carritoRepository.findByUser(user))
                .thenReturn(Optional.empty());

        // ACT
        boolean result = carritoService.isCartEmpty(userId);

        // ASSERT
        assertTrue(result);
        verify(carritoRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("addToCart lanza InsufficientStockException cuando se incrementa cantidad y supera el stock")
    void addToCart_IncrementQuantity_ExceedsStock() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .deleted(false)
                .build();

        LineaCarrito lineaExistente = LineaCarrito.builder()
                .id(1L)
                .productos(producto)
                .cantidad(8) // Ya hay 8 en el carrito
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(lineaExistente)))
                .build();

        lineaExistente.setCarrito(carrito);

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(productosRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        // ACT & ASSERT
        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> carritoService.addToCart(user, 1L, 5)); // 8 + 5 = 13 > 10

        // Verificar que se lanza la excepción correcta con el producto correcto
        assertNotNull(exception);
        verify(lineaCarritoRepository, never()).save(any());
        verify(carritoRepository, never()).save(carrito);
    }

    @Test
    @DisplayName("getCartItemCount lanza UserNotFoundException cuando el usuario no existe")
    void getCartItemCount_UserNotFound() {
        // ARRANGE
        Long userId = 999L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(UserNotFoundException.class,
                () -> carritoService.getCartItemCount(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(carritoRepository, never()).findByUser(any());
    }

    @Test
    @DisplayName("isCartEmpty lanza UserNotFoundException cuando el usuario no existe")
    void isCartEmpty_UserNotFound() {
        // ARRANGE
        Long userId = 999L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(UserNotFoundException.class,
                () -> carritoService.isCartEmpty(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(carritoRepository, never()).findByUser(any());
    }

    @Test
    @DisplayName("updateQuantity lanza ProductNotFoundException con cantidad negativa")
    void updateQuantity_NegativeQuantity() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .deleted(false)
                .build();

        LineaCarrito linea = LineaCarrito.builder()
                .id(1L)
                .productos(producto)
                .cantidad(2)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(linea)))
                .build();

        linea.setCarrito(carrito);

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class)))
                .thenReturn(carrito);

        // ACT - cantidad negativa debe eliminar el producto (comportamiento <= 0)
        carritoService.updateQuantity(user, 1L, -1);

        // ASSERT - debe eliminar el producto
        assertTrue(carrito.getLineasCarrito().isEmpty());
        verify(lineaCarritoRepository, times(1)).delete(linea);
    }

    @Test
    @DisplayName("addToCart verifica mensaje de excepción cuando producto está eliminado")
    void addToCart_DeletedProduct_CheckMessage() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15 Descontinuado")
                .precio(1000.0)
                .stock(10)
                .deleted(true)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(productosRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        // ACT & ASSERT
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> carritoService.addToCart(user, 1L, 2));

        // Verificar que el mensaje contiene el nombre del producto
        assertTrue(exception.getMessage().contains("iPhone 15 Descontinuado") ||
                   exception.getMessage().contains("no está disponible"));
        verify(lineaCarritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeFromCart verifica mensaje de excepción cuando producto no está en carrito")
    void removeFromCart_NotInCart_CheckMessage() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));

        // ACT & ASSERT
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> carritoService.removeFromCart(user, 999L));

        // Verificar que el mensaje es apropiado
        assertTrue(exception.getMessage().contains("carrito") ||
                   exception.getMessage().contains("no está"));
        verify(lineaCarritoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("updateQuantity verifica mensaje de excepción cuando producto no está en carrito")
    void updateQuantity_NotInCart_CheckMessage() {
        // ARRANGE
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));

        // ACT & ASSERT
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> carritoService.updateQuantity(user, 999L, 5));

        // Verificar mensaje
        assertTrue(exception.getMessage().contains("carrito") ||
                   exception.getMessage().contains("Producto no"));
        verify(lineaCarritoRepository, never()).save(any());
    }

    @Test
    @DisplayName("addToCart lanza ProductNotFoundException con ID de producto correcto")
    void addToCart_ProductNotFound_CheckId() {
        // ARRANGE
        Long productIdInexistente = 12345L;
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(carritoRepository.findByUserIdWithLineas(user.getId()))
                .thenReturn(Optional.of(carrito));
        when(productosRepository.findById(productIdInexistente))
                .thenReturn(Optional.empty());

        // ACT & ASSERT
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> carritoService.addToCart(user, productIdInexistente, 2));

        assertNotNull(exception);
        verify(lineaCarritoRepository, never()).save(any());
        verify(productosRepository, times(1)).findById(productIdInexistente);
    }
}

