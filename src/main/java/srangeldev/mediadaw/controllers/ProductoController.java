package srangeldev.mediadaw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import srangeldev.mediadaw.models.Productos;
import srangeldev.mediadaw.models.Categoria;
import srangeldev.mediadaw.services.ProductosService;

import java.util.List;

/**
 * Controlador para gestionar productos.
 * Muestra el catálogo de productos disponibles.
 */
@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductosService productosService;

    /**
     * Muestra todos los productos disponibles
     */
    @GetMapping
    public String listProducts(@RequestParam(required = false) Categoria category,
                              @RequestParam(required = false) String search,
                              Model model) {

        List<Productos> productos;

        if (search != null && !search.isEmpty()) {
            productos = productosService.searchProducts(search);
        } else if (category != null) {
            productos = productosService.getProductsByCategory(category);
        } else {
            productos = productosService.getAvailableProducts();
        }

        model.addAttribute("products", productos);
        model.addAttribute("categories", Categoria.values());
        model.addAttribute("selectedCategory", category != null ? category.name() : null);
        model.addAttribute("searchQuery", search);

        return "productos/listaProductos";
    }

    /**
     * Muestra los detalles de un producto específico
     */
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Productos productos = productosService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        model.addAttribute("product", productos);

        return "productos/detalleProducto";
    }
}
