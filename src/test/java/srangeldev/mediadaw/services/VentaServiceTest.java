package srangeldev.mediadaw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import srangeldev.mediadaw.exceptions.PurchaseNotFoundException;
import srangeldev.mediadaw.models.*;
import srangeldev.mediadaw.repositories.CarritoRepository;
import srangeldev.mediadaw.repositories.ProductosRepository;
import srangeldev.mediadaw.repositories.UserRepository;
import srangeldev.mediadaw.repositories.VentaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para VentaService.
 * Usamos MockitoExtension para no levantar todo el contexto de Spring (más rápido)
 */
@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductosRepository productosRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VentaService ventaService;

    @Test
    @DisplayName("createPurchaseFromCart crea un pedido correctamente desde el carrito")
    void createPurchaseFromCart() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@test.com").build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .deleted(false)
                .build();

        LineaCarrito lineaCarrito = LineaCarrito.builder()
                .id(1L)
                .productos(producto)
                .cantidad(2)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(lineaCarrito)))
                .build();

        lineaCarrito.setCarrito(carrito);

        Venta ventaGuardada = Venta.builder()
                .id(1L)
                .user(user)
                .estado(EstadoPedido.PENDIENTE)
                .total(2000.0)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(carritoRepository.findByUserId(userId)).thenReturn(Optional.of(carrito));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaGuardada);
        when(productosRepository.save(any(Productos.class))).thenReturn(producto);

        // ACT
        Venta result = ventaService.createPurchaseFromCart(userId);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(EstadoPedido.PENDIENTE, result.getEstado()),
                () -> assertEquals(user, result.getUser()),
                () -> assertEquals(2000.0, result.getTotal())
        );

        verify(userRepository, times(1)).findById(userId);
        verify(carritoRepository, times(1)).findByUserId(userId);
        verify(ventaRepository, times(1)).save(any(Venta.class));
        verify(productosRepository, times(1)).save(producto);
        verify(carritoRepository, times(1)).delete(carrito);
    }

    @Test
    @DisplayName("createPurchaseFromCart lanza excepción cuando el usuario no existe")
    void createPurchaseFromCart_UserNotFound() {
        // ARRANGE
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> ventaService.createPurchaseFromCart(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(carritoRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("createPurchaseFromCart lanza excepción cuando el carrito está vacío")
    void createPurchaseFromCart_EmptyCart() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@test.com").build();
        Carrito carritoVacio = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(carritoRepository.findByUserId(userId)).thenReturn(Optional.of(carritoVacio));

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> ventaService.createPurchaseFromCart(userId));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPurchaseFromCart lanza excepción cuando no hay stock suficiente")
    void createPurchaseFromCart_InsufficientStock() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@test.com").build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(1)
                .deleted(false)
                .build();

        LineaCarrito lineaCarrito = LineaCarrito.builder()
                .id(1L)
                .productos(producto)
                .cantidad(5)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(lineaCarrito)))
                .build();

        lineaCarrito.setCarrito(carrito);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(carritoRepository.findByUserId(userId)).thenReturn(Optional.of(carrito));

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> ventaService.createPurchaseFromCart(userId));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserPurchases devuelve lista de pedidos del usuario")
    void getUserPurchases() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@test.com").build();

        Venta venta1 = Venta.builder()
                .id(1L)
                .user(user)
                .estado(EstadoPedido.PENDIENTE)
                .total(100.0)
                .build();

        Venta venta2 = Venta.builder()
                .id(2L)
                .user(user)
                .estado(EstadoPedido.ENVIADO)
                .total(200.0)
                .build();

        when(ventaRepository.findByUserIdOrderByFechaCompraDesc(userId))
                .thenReturn(List.of(venta1, venta2));

        // ACT
        List<Venta> result = ventaService.getUserPurchases(userId);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertEquals(1L, result.get(0).getId()),
                () -> assertEquals(2L, result.get(1).getId())
        );

        verify(ventaRepository, times(1)).findByUserIdOrderByFechaCompraDesc(userId);
    }

    @Test
    @DisplayName("getPurchaseById devuelve el pedido cuando existe")
    void getPurchaseById() {
        // ARRANGE
        Long purchaseId = 1L;
        Venta venta = Venta.builder()
                .id(purchaseId)
                .estado(EstadoPedido.PENDIENTE)
                .total(100.0)
                .build();

        when(ventaRepository.findById(purchaseId)).thenReturn(Optional.of(venta));

        // ACT
        Optional<Venta> result = ventaService.getPurchaseById(purchaseId);

        // ASSERT
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(purchaseId, result.get().getId()),
                () -> assertEquals(100.0, result.get().getTotal())
        );

        verify(ventaRepository, times(1)).findById(purchaseId);
    }

    @Test
    @DisplayName("getPurchaseById devuelve Optional.empty cuando no existe")
    void getPurchaseById_NotFound() {
        // ARRANGE
        Long purchaseId = 999L;
        when(ventaRepository.findById(purchaseId)).thenReturn(Optional.empty());

        // ACT
        Optional<Venta> result = ventaService.getPurchaseById(purchaseId);

        // ASSERT
        assertFalse(result.isPresent());
        verify(ventaRepository, times(1)).findById(purchaseId);
    }

    @Test
    @DisplayName("updatePurchaseStatus actualiza el estado correctamente")
    void updatePurchaseStatus() {
        // ARRANGE
        Long purchaseId = 1L;
        Venta venta = Venta.builder()
                .id(purchaseId)
                .estado(EstadoPedido.PENDIENTE)
                .total(100.0)
                .build();

        Venta ventaActualizada = Venta.builder()
                .id(purchaseId)
                .estado(EstadoPedido.ENVIADO)
                .total(100.0)
                .build();

        when(ventaRepository.findById(purchaseId)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaActualizada);

        // ACT
        Venta result = ventaService.updatePurchaseStatus(purchaseId, EstadoPedido.ENVIADO);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(EstadoPedido.ENVIADO, result.getEstado()),
                () -> assertEquals(purchaseId, result.getId())
        );

        verify(ventaRepository, times(1)).findById(purchaseId);
        verify(ventaRepository, times(1)).save(venta);
    }

    @Test
    @DisplayName("updatePurchaseStatus lanza excepción cuando el pedido no existe")
    void updatePurchaseStatus_NotFound() {
        // ARRANGE
        Long purchaseId = 999L;
        when(ventaRepository.findById(purchaseId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class,
                () -> ventaService.updatePurchaseStatus(purchaseId, EstadoPedido.ENVIADO));
        verify(ventaRepository, times(1)).findById(purchaseId);
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("getPurchasesByStatus devuelve pedidos filtrados por estado")
    void getPurchasesByStatus() {
        // ARRANGE
        EstadoPedido estado = EstadoPedido.PENDIENTE;

        Venta venta1 = Venta.builder()
                .id(1L)
                .estado(EstadoPedido.PENDIENTE)
                .total(100.0)
                .build();

        Venta venta2 = Venta.builder()
                .id(2L)
                .estado(EstadoPedido.PENDIENTE)
                .total(200.0)
                .build();

        when(ventaRepository.findByEstadoOrderByFechaCompraDesc(estado))
                .thenReturn(List.of(venta1, venta2));

        // ACT
        List<Venta> result = ventaService.getPurchasesByStatus(estado);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().allMatch(v -> v.getEstado() == EstadoPedido.PENDIENTE))
        );

        verify(ventaRepository, times(1)).findByEstadoOrderByFechaCompraDesc(estado);
    }

    @Test
    @DisplayName("getAllPurchases devuelve todos los pedidos")
    void getAllPurchases() {
        // ARRANGE
        Venta venta1 = Venta.builder()
                .id(1L)
                .estado(EstadoPedido.PENDIENTE)
                .total(100.0)
                .build();

        Venta venta2 = Venta.builder()
                .id(2L)
                .estado(EstadoPedido.ENVIADO)
                .total(200.0)
                .build();

        Venta venta3 = Venta.builder()
                .id(3L)
                .estado(EstadoPedido.ENTREGADO)
                .total(300.0)
                .build();

        when(ventaRepository.findAll()).thenReturn(List.of(venta1, venta2, venta3));

        // ACT
        List<Venta> result = ventaService.getAllPurchases();

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(3, result.size())
        );

        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("cancelPurchase cancela el pedido y restaura el stock")
    void cancelPurchase() {
        // ARRANGE
        Long purchaseId = 1L;

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .build();

        LineaVenta lineaVenta = LineaVenta.builder()
                .id(1L)
                .productos(producto)
                .cantidad(2)
                .precioVenta(1000.0)
                .build();

        Venta venta = Venta.builder()
                .id(purchaseId)
                .estado(EstadoPedido.PENDIENTE)
                .total(2000.0)
                .lines(new ArrayList<>(List.of(lineaVenta)))
                .build();

        lineaVenta.setVenta(venta);

        when(ventaRepository.findById(purchaseId)).thenReturn(Optional.of(venta));
        when(productosRepository.save(any(Productos.class))).thenReturn(producto);

        // ACT
        ventaService.cancelPurchase(purchaseId);

        // ASSERT
        verify(ventaRepository, times(1)).findById(purchaseId);
        verify(productosRepository, times(1)).save(producto);
        verify(ventaRepository, times(1)).delete(venta);
    }

    @Test
    @DisplayName("cancelPurchase lanza excepción cuando el pedido no existe")
    void cancelPurchase_NotFound() {
        // ARRANGE
        Long purchaseId = 999L;
        when(ventaRepository.findById(purchaseId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(PurchaseNotFoundException.class,
                () -> ventaService.cancelPurchase(purchaseId));
        verify(ventaRepository, times(1)).findById(purchaseId);
        verify(ventaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("cancelPurchase lanza excepción cuando el pedido no está PENDIENTE")
    void cancelPurchase_NotPending() {
        // ARRANGE
        Long purchaseId = 1L;
        Venta venta = Venta.builder()
                .id(purchaseId)
                .estado(EstadoPedido.ENVIADO)
                .total(100.0)
                .build();

        when(ventaRepository.findById(purchaseId)).thenReturn(Optional.of(venta));

        // ACT & ASSERT
        assertThrows(IllegalStateException.class,
                () -> ventaService.cancelPurchase(purchaseId));
        verify(ventaRepository, times(1)).findById(purchaseId);
        verify(ventaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("calculateTotalRevenue calcula el total de ingresos correctamente")
    void calculateTotalRevenue() {
        // ARRANGE
        Venta venta1 = Venta.builder()
                .id(1L)
                .estado(EstadoPedido.ENTREGADO)
                .total(100.0)
                .build();

        Venta venta2 = Venta.builder()
                .id(2L)
                .estado(EstadoPedido.ENTREGADO)
                .total(250.0)
                .build();

        Venta venta3 = Venta.builder()
                .id(3L)
                .estado(EstadoPedido.ENVIADO)
                .total(150.0)
                .build();

        when(ventaRepository.findAll()).thenReturn(List.of(venta1, venta2, venta3));

        // ACT
        Double result = ventaService.calculateTotalRevenue();

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(500.0, result)
        );

        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("calculateTotalRevenue devuelve 0.0 cuando no hay ventas")
    void calculateTotalRevenue_NoSales() {
        // ARRANGE
        when(ventaRepository.findAll()).thenReturn(List.of());

        // ACT
        Double result = ventaService.calculateTotalRevenue();

        // ASSERT
        assertEquals(0.0, result);
        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("countByStatus cuenta pedidos por estado correctamente")
    void countByStatus() {
        // ARRANGE
        EstadoPedido estado = EstadoPedido.PENDIENTE;
        when(ventaRepository.countByEstado(estado)).thenReturn(5L);

        // ACT
        long result = ventaService.countByStatus(estado);

        // ASSERT
        assertEquals(5L, result);
        verify(ventaRepository, times(1)).countByEstado(estado);
    }

    @Test
    @DisplayName("countByStatus devuelve 0 cuando no hay pedidos con ese estado")
    void countByStatus_NoOrders() {
        // ARRANGE
        EstadoPedido estado = EstadoPedido.ENTREGADO;
        when(ventaRepository.countByEstado(estado)).thenReturn(0L);

        // ACT
        long result = ventaService.countByStatus(estado);

        // ASSERT
        assertEquals(0L, result);
        verify(ventaRepository, times(1)).countByEstado(estado);
    }

    // ============================================================
    // TESTS ADICIONALES DE EXCEPCIONES
    // ============================================================

    @Test
    @DisplayName("createPurchaseFromCart lanza RuntimeException con mensaje verificable cuando usuario no existe")
    void createPurchaseFromCart_UserNotFound_CheckMessage() {
        // ARRANGE
        Long userId = 88888L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ventaService.createPurchaseFromCart(userId));

        // Verificar que el mensaje contiene información útil
        assertTrue(exception.getMessage().contains("Usuario") ||
                   exception.getMessage().contains("usuario"));
        verify(userRepository, times(1)).findById(userId);
        verify(carritoRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("createPurchaseFromCart lanza RuntimeException cuando el carrito no existe")
    void createPurchaseFromCart_CartNotFound() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@test.com").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(carritoRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ventaService.createPurchaseFromCart(userId));

        assertTrue(exception.getMessage().contains("carrito") ||
                   exception.getMessage().contains("Carrito"));
        verify(userRepository, times(1)).findById(userId);
        verify(carritoRepository, times(1)).findByUserId(userId);
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPurchaseFromCart lanza RuntimeException con mensaje de stock insuficiente")
    void createPurchaseFromCart_InsufficientStock_CheckMessage() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@test.com").build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("iPhone 15 Pro Max")
                .precio(1200.0)
                .stock(2)
                .deleted(false)
                .build();

        LineaCarrito lineaCarrito = LineaCarrito.builder()
                .id(1L)
                .productos(producto)
                .cantidad(10) // Más de lo disponible
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(lineaCarrito)))
                .build();

        lineaCarrito.setCarrito(carrito);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(carritoRepository.findByUserId(userId)).thenReturn(Optional.of(carrito));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ventaService.createPurchaseFromCart(userId));

        // Verificar que el mensaje menciona el stock o el producto
        assertTrue(exception.getMessage().contains("Stock") ||
                   exception.getMessage().contains("stock") ||
                   exception.getMessage().contains("iPhone 15 Pro Max"));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPurchaseFromCart lanza RuntimeException cuando producto está eliminado")
    void createPurchaseFromCart_DeletedProduct() {
        // ARRANGE
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@test.com").build();

        Productos producto = Productos.builder()
                .id(1L)
                .nombre("Producto Descontinuado")
                .precio(500.0)
                .stock(10)
                .deleted(true) // Producto eliminado
                .build();

        LineaCarrito lineaCarrito = LineaCarrito.builder()
                .id(1L)
                .productos(producto)
                .cantidad(2)
                .build();

        Carrito carrito = Carrito.builder()
                .id(1L)
                .user(user)
                .lineasCarrito(new ArrayList<>(List.of(lineaCarrito)))
                .build();

        lineaCarrito.setCarrito(carrito);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(carritoRepository.findByUserId(userId)).thenReturn(Optional.of(carrito));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ventaService.createPurchaseFromCart(userId));

        // Verificar que el mensaje menciona disponibilidad
        assertTrue(exception.getMessage().contains("disponible") ||
                   exception.getMessage().contains("Producto Descontinuado"));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePurchaseStatus lanza RuntimeException con mensaje verificable")
    void updatePurchaseStatus_NotFound_CheckMessage() {
        // ARRANGE
        Long purchaseId = 77777L;
        when(ventaRepository.findById(purchaseId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> ventaService.updatePurchaseStatus(purchaseId, EstadoPedido.ENVIADO));

        // Verificar que el mensaje es útil
        assertTrue(exception.getMessage().contains("Pedido") ||
                   exception.getMessage().contains("pedido") ||
                   exception.getMessage().contains("encontrado"));
        verify(ventaRepository, times(1)).findById(purchaseId);
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelPurchase lanza PurchaseNotFoundException con ID verificable")
    void cancelPurchase_NotFound_CheckId() {
        // ARRANGE
        Long purchaseIdInexistente = 66666L;
        when(ventaRepository.findById(purchaseIdInexistente)).thenReturn(Optional.empty());

        // ACT & ASSERT
        PurchaseNotFoundException exception = assertThrows(PurchaseNotFoundException.class,
                () -> ventaService.cancelPurchase(purchaseIdInexistente));

        assertNotNull(exception);
        verify(ventaRepository, times(1)).findById(purchaseIdInexistente);
        verify(ventaRepository, never()).delete(any());
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelPurchase lanza IllegalStateException con mensaje verificable cuando no está PENDIENTE")
    void cancelPurchase_NotPending_CheckMessage() {
        // ARRANGE
        Long purchaseId = 1L;
        Venta venta = Venta.builder()
                .id(purchaseId)
                .estado(EstadoPedido.ENTREGADO) // Ya entregado
                .total(500.0)
                .build();

        when(ventaRepository.findById(purchaseId)).thenReturn(Optional.of(venta));

        // ACT & ASSERT
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ventaService.cancelPurchase(purchaseId));

        // Verificar que el mensaje explica el problema
        assertTrue(exception.getMessage().contains("pendiente") ||
                   exception.getMessage().contains("PENDIENTE") ||
                   exception.getMessage().contains("cancelar"));
        verify(ventaRepository, times(1)).findById(purchaseId);
        verify(ventaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getAllPurchases devuelve lista vacía cuando no hay pedidos")
    void getAllPurchases_Empty() {
        // ARRANGE
        when(ventaRepository.findAll()).thenReturn(List.of());

        // ACT
        List<Venta> result = ventaService.getAllPurchases();

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getPurchasesByStatus devuelve lista vacía cuando no hay pedidos con ese estado")
    void getPurchasesByStatus_Empty() {
        // ARRANGE
        EstadoPedido estado = EstadoPedido.PENDIENTE;
        when(ventaRepository.findByEstadoOrderByFechaCompraDesc(estado)).thenReturn(List.of());

        // ACT
        List<Venta> result = ventaService.getPurchasesByStatus(estado);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

        verify(ventaRepository, times(1)).findByEstadoOrderByFechaCompraDesc(estado);
    }

    @Test
    @DisplayName("getUserPurchases devuelve lista vacía cuando el usuario no tiene pedidos")
    void getUserPurchases_Empty() {
        // ARRANGE
        Long userId = 1L;
        when(ventaRepository.findByUserIdOrderByFechaCompraDesc(userId)).thenReturn(List.of());

        // ACT
        List<Venta> result = ventaService.getUserPurchases(userId);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

        verify(ventaRepository, times(1)).findByUserIdOrderByFechaCompraDesc(userId);
    }

    @Test
    @DisplayName("updatePurchaseStatus actualiza a todos los estados sin excepción")
    void updatePurchaseStatus_AllStates() {
        // ARRANGE
        Long purchaseId = 1L;

        // Test PENDIENTE -> ENVIADO
        Venta ventaPendiente = Venta.builder()
                .id(purchaseId)
                .estado(EstadoPedido.PENDIENTE)
                .total(100.0)
                .build();

        when(ventaRepository.findById(purchaseId)).thenReturn(Optional.of(ventaPendiente));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT & ASSERT - ENVIADO
        Venta resultEnviado = ventaService.updatePurchaseStatus(purchaseId, EstadoPedido.ENVIADO);
        assertEquals(EstadoPedido.ENVIADO, resultEnviado.getEstado());

        // ARRANGE - ENVIADO -> ENTREGADO
        Venta ventaEnviada = Venta.builder()
                .id(purchaseId)
                .estado(EstadoPedido.ENVIADO)
                .total(100.0)
                .build();

        when(ventaRepository.findById(purchaseId)).thenReturn(Optional.of(ventaEnviada));

        // ACT & ASSERT - ENTREGADO
        Venta resultEntregado = ventaService.updatePurchaseStatus(purchaseId, EstadoPedido.ENTREGADO);
        assertEquals(EstadoPedido.ENTREGADO, resultEntregado.getEstado());

        verify(ventaRepository, times(2)).findById(purchaseId);
        verify(ventaRepository, times(2)).save(any(Venta.class));
    }

    @Test
    @DisplayName("cancelPurchase restaura stock correctamente con múltiples productos")
    void cancelPurchase_MultipleProducts() {
        // ARRANGE
        Long purchaseId = 1L;

        Productos producto1 = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .build();

        Productos producto2 = Productos.builder()
                .id(2L)
                .nombre("iPad Pro")
                .precio(1200.0)
                .stock(5)
                .build();

        LineaVenta linea1 = LineaVenta.builder()
                .id(1L)
                .productos(producto1)
                .cantidad(2)
                .precioVenta(1000.0)
                .build();

        LineaVenta linea2 = LineaVenta.builder()
                .id(2L)
                .productos(producto2)
                .cantidad(3)
                .precioVenta(1200.0)
                .build();

        Venta venta = Venta.builder()
                .id(purchaseId)
                .estado(EstadoPedido.PENDIENTE)
                .total(5600.0)
                .lines(new ArrayList<>(List.of(linea1, linea2)))
                .build();

        linea1.setVenta(venta);
        linea2.setVenta(venta);

        when(ventaRepository.findById(purchaseId)).thenReturn(Optional.of(venta));
        when(productosRepository.save(any(Productos.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        ventaService.cancelPurchase(purchaseId);

        // ASSERT
        verify(ventaRepository, times(1)).findById(purchaseId);
        verify(productosRepository, times(2)).save(any(Productos.class)); // Dos productos
        verify(ventaRepository, times(1)).delete(venta);
    }
}

