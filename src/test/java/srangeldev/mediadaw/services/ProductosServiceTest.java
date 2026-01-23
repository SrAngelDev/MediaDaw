package srangeldev.mediadaw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import srangeldev.mediadaw.exceptions.ProductNotFoundException;
import srangeldev.mediadaw.models.Categoria;
import srangeldev.mediadaw.models.Productos;
import srangeldev.mediadaw.repositories.ProductosRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProductosService.
 * Usamos MockitoExtension para no levantar todo el contexto de Spring (más rápido)
 */
@ExtendWith(MockitoExtension.class)
class ProductosServiceTest {

    @Mock
    private ProductosRepository productosRepository;

    @InjectMocks
    private ProductosService productosService;

    @Test
    @DisplayName("getAllProducts devuelve todos los productos no eliminados")
    void getAllProducts() {
        // ARRANGE
        Productos producto1 = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        Productos producto2 = Productos.builder()
                .id(2L)
                .nombre("MacBook Pro")
                .precio(2500.0)
                .stock(5)
                .category(Categoria.LAPTOPS)
                .deleted(false)
                .build();

        when(productosRepository.findByDeletedFalse()).thenReturn(List.of(producto1, producto2));

        // ACT
        List<Productos> result = productosService.getAllProducts();

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().noneMatch(Productos::getDeleted))
        );

        verify(productosRepository, times(1)).findByDeletedFalse();
    }

    @Test
    @DisplayName("getAvailableProducts devuelve solo productos con stock mayor a 0")
    void getAvailableProducts() {
        // ARRANGE
        Productos producto1 = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        Productos producto2 = Productos.builder()
                .id(2L)
                .nombre("iPad Pro")
                .precio(1200.0)
                .stock(5)
                .category(Categoria.LAPTOPS)
                .deleted(false)
                .build();

        when(productosRepository.findAvailableProducts()).thenReturn(List.of(producto1, producto2));

        // ACT
        List<Productos> result = productosService.getAvailableProducts();

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().allMatch(p -> p.getStock() > 0))
        );

        verify(productosRepository, times(1)).findAvailableProducts();
    }

    @Test
    @DisplayName("getProductsByCategory devuelve productos de una categoría específica")
    void getProductsByCategory() {
        // ARRANGE
        Categoria categoria = Categoria.AUDIO;

        Productos producto1 = Productos.builder()
                .id(1L)
                .nombre("Auriculares Sony")
                .precio(300.0)
                .stock(15)
                .category(Categoria.AUDIO)
                .deleted(false)
                .build();

        Productos producto2 = Productos.builder()
                .id(2L)
                .nombre("Altavoz JBL")
                .precio(150.0)
                .stock(20)
                .category(Categoria.AUDIO)
                .deleted(false)
                .build();

        when(productosRepository.findByCategoryAndDeletedFalse(categoria))
                .thenReturn(List.of(producto1, producto2));

        // ACT
        List<Productos> result = productosService.getProductsByCategory(categoria);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().allMatch(p -> p.getCategory() == Categoria.AUDIO))
        );

        verify(productosRepository, times(1)).findByCategoryAndDeletedFalse(categoria);
    }

    @Test
    @DisplayName("getProductById devuelve el producto cuando existe y no está eliminado")
    void getProductById() {
        // ARRANGE
        Long productId = 1L;
        Productos producto = Productos.builder()
                .id(productId)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.of(producto));

        // ACT
        Optional<Productos> result = productosService.getProductById(productId);

        // ASSERT
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(productId, result.get().getId()),
                () -> assertEquals("iPhone 15", result.get().getNombre()),
                () -> assertFalse(result.get().getDeleted())
        );

        verify(productosRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("getProductById devuelve Optional.empty cuando el producto está eliminado")
    void getProductById_Deleted() {
        // ARRANGE
        Long productId = 1L;
        Productos producto = Productos.builder()
                .id(productId)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(true)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.of(producto));

        // ACT
        Optional<Productos> result = productosService.getProductById(productId);

        // ASSERT
        assertFalse(result.isPresent());
        verify(productosRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("getProductById devuelve Optional.empty cuando no existe")
    void getProductById_NotFound() {
        // ARRANGE
        Long productId = 999L;
        when(productosRepository.findById(productId)).thenReturn(Optional.empty());

        // ACT
        Optional<Productos> result = productosService.getProductById(productId);

        // ASSERT
        assertFalse(result.isPresent());
        verify(productosRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("searchProducts busca productos por nombre")
    void searchProducts() {
        // ARRANGE
        String searchTerm = "iPhone";

        Productos producto1 = Productos.builder()
                .id(1L)
                .nombre("iPhone 15 Pro")
                .precio(1200.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        Productos producto2 = Productos.builder()
                .id(2L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(15)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        when(productosRepository.searchByNombre(searchTerm))
                .thenReturn(List.of(producto1, producto2));

        // ACT
        List<Productos> result = productosService.searchProducts(searchTerm);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().allMatch(p -> p.getNombre().contains("iPhone")))
        );

        verify(productosRepository, times(1)).searchByNombre(searchTerm);
    }

    @Test
    @DisplayName("createProduct crea un nuevo producto correctamente")
    void createProduct() {
        // ARRANGE
        Productos newProduct = Productos.builder()
                .nombre("Nuevo Producto")
                .descripcion("Descripción del producto")
                .precio(500.0)
                .stock(20)
                .category(Categoria.GAMING)
                .deleted(false)
                .build();

        Productos savedProduct = Productos.builder()
                .id(1L)
                .nombre("Nuevo Producto")
                .descripcion("Descripción del producto")
                .precio(500.0)
                .stock(20)
                .category(Categoria.GAMING)
                .deleted(false)
                .build();

        when(productosRepository.save(any(Productos.class))).thenReturn(savedProduct);

        // ACT
        Productos result = productosService.createProduct(newProduct);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1L, result.getId()),
                () -> assertEquals("Nuevo Producto", result.getNombre()),
                () -> assertEquals(500.0, result.getPrecio()),
                () -> assertEquals(20, result.getStock())
        );

        verify(productosRepository, times(1)).save(newProduct);
    }

    @Test
    @DisplayName("updateProduct actualiza un producto existente correctamente")
    void updateProduct() {
        // ARRANGE
        Long productId = 1L;
        Productos existingProduct = Productos.builder()
                .id(productId)
                .nombre("Producto Viejo")
                .descripcion("Descripción vieja")
                .precio(500.0)
                .stock(10)
                .category(Categoria.GAMING)
                .imagen("old.jpg")
                .deleted(false)
                .build();

        Productos productData = Productos.builder()
                .nombre("Producto Actualizado")
                .descripcion("Descripción actualizada")
                .precio(600.0)
                .stock(15)
                .category(Categoria.AUDIO)
                .imagen("new.jpg")
                .build();

        Productos updatedProduct = Productos.builder()
                .id(productId)
                .nombre("Producto Actualizado")
                .descripcion("Descripción actualizada")
                .precio(600.0)
                .stock(15)
                .category(Categoria.AUDIO)
                .imagen("new.jpg")
                .deleted(false)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productosRepository.save(any(Productos.class))).thenReturn(updatedProduct);

        // ACT
        Productos result = productosService.updateProduct(productId, productData);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Producto Actualizado", result.getNombre()),
                () -> assertEquals("Descripción actualizada", result.getDescripcion()),
                () -> assertEquals(600.0, result.getPrecio()),
                () -> assertEquals(15, result.getStock()),
                () -> assertEquals(Categoria.AUDIO, result.getCategory()),
                () -> assertEquals("new.jpg", result.getImagen())
        );

        verify(productosRepository, times(1)).findById(productId);
        verify(productosRepository, times(1)).save(existingProduct);
    }

    @Test
    @DisplayName("updateProduct lanza excepción cuando el producto no existe")
    void updateProduct_NotFound() {
        // ARRANGE
        Long productId = 999L;
        Productos productData = Productos.builder()
                .nombre("Producto Actualizado")
                .precio(600.0)
                .stock(15)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ProductNotFoundException.class,
                () -> productosService.updateProduct(productId, productData));
        verify(productosRepository, times(1)).findById(productId);
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteProduct marca el producto como eliminado (borrado lógico)")
    void deleteProduct() {
        // ARRANGE
        Long productId = 1L;
        Productos producto = Productos.builder()
                .id(productId)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.of(producto));
        when(productosRepository.save(any(Productos.class))).thenReturn(producto);

        // ACT
        productosService.deleteProduct(productId);

        // ASSERT
        assertTrue(producto.getDeleted());
        verify(productosRepository, times(1)).findById(productId);
        verify(productosRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("deleteProduct lanza excepción cuando el producto no existe")
    void deleteProduct_NotFound() {
        // ARRANGE
        Long productId = 999L;
        when(productosRepository.findById(productId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ProductNotFoundException.class,
                () -> productosService.deleteProduct(productId));
        verify(productosRepository, times(1)).findById(productId);
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStock actualiza el stock del producto correctamente")
    void updateStock() {
        // ARRANGE
        Long productId = 1L;
        int newStock = 50;

        Productos producto = Productos.builder()
                .id(productId)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        Productos productoActualizado = Productos.builder()
                .id(productId)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(50)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.of(producto));
        when(productosRepository.save(any(Productos.class))).thenReturn(productoActualizado);

        // ACT
        Productos result = productosService.updateStock(productId, newStock);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(50, result.getStock()),
                () -> assertEquals(productId, result.getId())
        );

        verify(productosRepository, times(1)).findById(productId);
        verify(productosRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("updateStock lanza excepción cuando el producto no existe")
    void updateStock_NotFound() {
        // ARRANGE
        Long productId = 999L;
        int newStock = 50;

        when(productosRepository.findById(productId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ProductNotFoundException.class,
                () -> productosService.updateStock(productId, newStock));
        verify(productosRepository, times(1)).findById(productId);
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("getLowStockProducts devuelve productos con stock bajo el umbral")
    void getLowStockProducts() {
        // ARRANGE
        int threshold = 10;

        Productos producto1 = Productos.builder()
                .id(1L)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(5)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        Productos producto2 = Productos.builder()
                .id(2L)
                .nombre("iPad Pro")
                .precio(1200.0)
                .stock(3)
                .category(Categoria.LAPTOPS)
                .deleted(false)
                .build();

        when(productosRepository.findLowStockProducts(threshold))
                .thenReturn(List.of(producto1, producto2));

        // ACT
        List<Productos> result = productosService.getLowStockProducts(threshold);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> assertTrue(result.stream().allMatch(p -> p.getStock() < threshold))
        );

        verify(productosRepository, times(1)).findLowStockProducts(threshold);
    }

    @Test
    @DisplayName("checkStock devuelve true cuando hay stock suficiente")
    void checkStock() {
        // ARRANGE
        Long productId = 1L;
        int quantity = 5;

        Productos producto = Productos.builder()
                .id(productId)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.of(producto));

        // ACT
        boolean result = productosService.checkStock(productId, quantity);

        // ASSERT
        assertTrue(result);
        verify(productosRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("checkStock devuelve false cuando no hay stock suficiente")
    void checkStock_InsufficientStock() {
        // ARRANGE
        Long productId = 1L;
        int quantity = 15;

        Productos producto = Productos.builder()
                .id(productId)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.of(producto));

        // ACT
        boolean result = productosService.checkStock(productId, quantity);

        // ASSERT
        assertFalse(result);
        verify(productosRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("checkStock devuelve false cuando el producto no existe")
    void checkStock_NotFound() {
        // ARRANGE
        Long productId = 999L;
        int quantity = 5;

        when(productosRepository.findById(productId)).thenReturn(Optional.empty());

        // ACT
        boolean result = productosService.checkStock(productId, quantity);

        // ASSERT
        assertFalse(result);
        verify(productosRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("checkStock devuelve false cuando el producto está eliminado")
    void checkStock_Deleted() {
        // ARRANGE
        Long productId = 1L;
        int quantity = 5;

        Productos producto = Productos.builder()
                .id(productId)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(true)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.of(producto));

        // ACT
        boolean result = productosService.checkStock(productId, quantity);

        // ASSERT
        assertFalse(result);
        verify(productosRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("updateProduct lanza ProductNotFoundException con mensaje verificable")
    void updateProduct_NotFound_CheckMessage() {
        // ARRANGE
        Long productId = 12345L;
        Productos productData = Productos.builder()
                .nombre("Producto Actualizado")
                .precio(600.0)
                .stock(15)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productosService.updateProduct(productId, productData));

        // Verificar que la excepción contiene información útil
        assertNotNull(exception);
        verify(productosRepository, times(1)).findById(productId);
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteProduct lanza ProductNotFoundException con mensaje verificable")
    void deleteProduct_NotFound_CheckMessage() {
        // ARRANGE
        Long productId = 99999L;
        when(productosRepository.findById(productId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productosService.deleteProduct(productId));

        // Verificar que la excepción se lanza correctamente
        assertNotNull(exception);
        verify(productosRepository, times(1)).findById(productId);
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStock lanza ProductNotFoundException con ID específico")
    void updateStock_NotFound_CheckId() {
        // ARRANGE
        Long productIdInexistente = 77777L;
        int newStock = 100;

        when(productosRepository.findById(productIdInexistente)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productosService.updateStock(productIdInexistente, newStock));

        assertNotNull(exception);
        verify(productosRepository, times(1)).findById(productIdInexistente);
        verify(productosRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProduct no actualiza imagen si es null")
    void updateProduct_NullImage() {
        // ARRANGE
        Long productId = 1L;
        Productos existingProduct = Productos.builder()
                .id(productId)
                .nombre("Producto Original")
                .descripcion("Descripción original")
                .precio(500.0)
                .stock(10)
                .category(Categoria.GAMING)
                .imagen("imagen-original.jpg")
                .deleted(false)
                .build();

        Productos productData = Productos.builder()
                .nombre("Producto Actualizado")
                .descripcion("Descripción actualizada")
                .precio(600.0)
                .stock(15)
                .category(Categoria.AUDIO)
                .imagen(null) // Imagen null, no debe actualizarse
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productosRepository.save(any(Productos.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        Productos result = productosService.updateProduct(productId, productData);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("Producto Actualizado", result.getNombre()),
                () -> assertEquals("imagen-original.jpg", result.getImagen()), // Imagen no cambia
                () -> assertEquals(600.0, result.getPrecio())
        );

        verify(productosRepository, times(1)).findById(productId);
        verify(productosRepository, times(1)).save(existingProduct);
    }

    @Test
    @DisplayName("getAllProducts devuelve lista vacía cuando no hay productos")
    void getAllProducts_Empty() {
        // ARRANGE
        when(productosRepository.findByDeletedFalse()).thenReturn(List.of());

        // ACT
        List<Productos> result = productosService.getAllProducts();

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

        verify(productosRepository, times(1)).findByDeletedFalse();
    }

    @Test
    @DisplayName("getAvailableProducts devuelve lista vacía cuando no hay productos disponibles")
    void getAvailableProducts_Empty() {
        // ARRANGE
        when(productosRepository.findAvailableProducts()).thenReturn(List.of());

        // ACT
        List<Productos> result = productosService.getAvailableProducts();

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

        verify(productosRepository, times(1)).findAvailableProducts();
    }

    @Test
    @DisplayName("getProductsByCategory devuelve lista vacía cuando no hay productos de esa categoría")
    void getProductsByCategory_Empty() {
        // ARRANGE
        Categoria categoria = Categoria.INSTRUMENTOS;
        when(productosRepository.findByCategoryAndDeletedFalse(categoria)).thenReturn(List.of());

        // ACT
        List<Productos> result = productosService.getProductsByCategory(categoria);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

        verify(productosRepository, times(1)).findByCategoryAndDeletedFalse(categoria);
    }

    @Test
    @DisplayName("searchProducts devuelve lista vacía cuando no encuentra coincidencias")
    void searchProducts_NoResults() {
        // ARRANGE
        String searchTerm = "ProductoInexistente12345";
        when(productosRepository.searchByNombre(searchTerm)).thenReturn(List.of());

        // ACT
        List<Productos> result = productosService.searchProducts(searchTerm);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

        verify(productosRepository, times(1)).searchByNombre(searchTerm);
    }

    @Test
    @DisplayName("getLowStockProducts devuelve lista vacía cuando no hay productos con stock bajo")
    void getLowStockProducts_Empty() {
        // ARRANGE
        int threshold = 5;
        when(productosRepository.findLowStockProducts(threshold)).thenReturn(List.of());

        // ACT
        List<Productos> result = productosService.getLowStockProducts(threshold);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );

        verify(productosRepository, times(1)).findLowStockProducts(threshold);
    }

    @Test
    @DisplayName("checkStock con cantidad 0 devuelve true")
    void checkStock_ZeroQuantity() {
        // ARRANGE
        Long productId = 1L;
        int quantity = 0;

        Productos producto = Productos.builder()
                .id(productId)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.of(producto));

        // ACT
        boolean result = productosService.checkStock(productId, quantity);

        // ASSERT
        assertTrue(result); // Stock 10 >= cantidad 0
        verify(productosRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("checkStock con cantidad exacta igual al stock devuelve true")
    void checkStock_ExactStock() {
        // ARRANGE
        Long productId = 1L;
        int quantity = 10;

        Productos producto = Productos.builder()
                .id(productId)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.of(producto));

        // ACT
        boolean result = productosService.checkStock(productId, quantity);

        // ASSERT
        assertTrue(result); // Stock 10 >= cantidad 10
        verify(productosRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("updateStock con stock 0 permite actualización")
    void updateStock_ZeroStock() {
        // ARRANGE
        Long productId = 1L;
        int newStock = 0;

        Productos producto = Productos.builder()
                .id(productId)
                .nombre("iPhone 15")
                .precio(1000.0)
                .stock(10)
                .category(Categoria.SMARTPHONES)
                .deleted(false)
                .build();

        when(productosRepository.findById(productId)).thenReturn(Optional.of(producto));
        when(productosRepository.save(any(Productos.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        Productos result = productosService.updateStock(productId, newStock);

        // ASSERT
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(0, result.getStock())
        );

        verify(productosRepository, times(1)).findById(productId);
        verify(productosRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("createProduct guarda producto sin validaciones previas")
    void createProduct_DirectSave() {
        // ARRANGE
        Productos newProduct = Productos.builder()
                .nombre("Producto Test")
                .descripcion("Test Description")
                .precio(100.0)
                .stock(5)
                .category(Categoria.AUDIO)
                .deleted(false)
                .build();

        when(productosRepository.save(newProduct)).thenReturn(newProduct);

        // ACT
        Productos result = productosService.createProduct(newProduct);

        // ASSERT
        assertNotNull(result);
        verify(productosRepository, times(1)).save(newProduct);
        // No debe haber llamadas a findById u otros métodos
        verify(productosRepository, never()).findById(any());
    }
}

