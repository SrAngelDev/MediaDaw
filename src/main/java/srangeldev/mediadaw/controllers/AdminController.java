package srangeldev.mediadaw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import srangeldev.mediadaw.models.Productos;
import srangeldev.mediadaw.models.Categoria;
import srangeldev.mediadaw.models.EstadoPedido;
import srangeldev.mediadaw.services.ProductosService;
import srangeldev.mediadaw.services.VentaService;

/**
 * Controlador del panel de administración (solo ADMIN).
 * Permite gestionar productos, stock y pedidos.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ProductosService productosService;
    private final VentaService ventaService;

    /**
     * Dashboard principal del administrador
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productosService.getAllProducts().size());
        model.addAttribute("lowStockProducts", productosService.getLowStockProducts(10));
        model.addAttribute("pendingOrders", ventaService.countByStatus(EstadoPedido.PENDIENTE));
        model.addAttribute("totalRevenue", ventaService.calculateTotalRevenue());

        return "admin/dashboard"; // Vista: admin/dashboard.peb.html
    }

    /**
     * Lista de todos los productos (incluidos eliminados)
     */
    @GetMapping("/productos")
    public String listAllProducts(Model model) {
        model.addAttribute("products", productosService.getAllProducts());
        return "admin/productos/list";
    }

    /**
     * Formulario para crear nuevo producto
     */
    @GetMapping("/productos/nuevo")
    public String newProductForm(Model model) {
        model.addAttribute("categories", Categoria.values());
        return "admin/productos/form";
    }

    /**
     * Guardar nuevo producto
     */
    @PostMapping("/productos")
    public String createProduct(@ModelAttribute Productos productos) {
        productosService.createProduct(productos);
        return "redirect:/admin/productos";
    }

    /**
     * Formulario para editar producto existente
     */
    @GetMapping("/productos/{id}/editar")
    public String editProductForm(@PathVariable Long id, Model model) {
        Productos productos = productosService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        model.addAttribute("product", productos);
        model.addAttribute("categories", Categoria.values());

        return "admin/productos/form";
    }

    /**
     * Actualizar producto existente
     */
    @PostMapping("/productos/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Productos productos) {
        productosService.updateProduct(id, productos);
        return "redirect:/admin/productos";
    }

    /**
     * Eliminar producto (borrado lógico)
     */
    @PostMapping("/productos/{id}/eliminar")
    public String deleteProduct(@PathVariable Long id) {
        productosService.deleteProduct(id);
        return "redirect:/admin/productos";
    }

    /**
     * Lista de todos los pedidos
     */
    @GetMapping("/pedidos")
    public String listAllPurchases(Model model) {
        model.addAttribute("purchases", ventaService.getAllPurchases());
        model.addAttribute("statuses", EstadoPedido.values());
        return "admin/pedidos/list";
    }

    /**
     * Actualizar estado de un pedido
     */
    @PostMapping("/pedidos/{id}/estado")
    public String updatePurchaseStatus(@PathVariable Long id, @RequestParam EstadoPedido status) {
        ventaService.updatePurchaseStatus(id, status);
        return "redirect:/admin/pedidos";
    }
}
