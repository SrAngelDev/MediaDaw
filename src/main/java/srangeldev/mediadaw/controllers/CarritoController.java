package srangeldev.mediadaw.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import srangeldev.mediadaw.exceptions.InsufficientStockException;
import srangeldev.mediadaw.exceptions.ProductNotFoundException;
import srangeldev.mediadaw.models.Carrito;
import srangeldev.mediadaw.models.User;
import srangeldev.mediadaw.repositories.UserRepository;
import srangeldev.mediadaw.services.CarritoService;

import java.security.Principal;

@Controller
@RequestMapping("/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;
    private final UserRepository userRepository;
    // private final PurchaseService purchaseService; // Descomentar cuando implementes la compra

    /**
     * Helper para obtener el usuario real de la BBDD desde la sesión de seguridad
     */
    private User getAuthenticatedUser(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en sesión"));
    }

    /**
     * GET /carrito - Ver el carrito
     */
    @GetMapping
    public String viewCart(Model model, Principal principal) {
        User user = getAuthenticatedUser(principal);
        Carrito carrito = carritoService.getCart(user);

        model.addAttribute("carrito", carrito);
        model.addAttribute("items", carrito.getLineasCarrito());
        model.addAttribute("total", carrito.getTotal());

        return "carrito/carrito";
    }

    /**
     * POST /carrito/add - Añadir producto
     */
    @PostMapping("/add")
    public String addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User user = getAuthenticatedUser(principal);

        try {
            carritoService.addToCart(user, productId, quantity);
            redirectAttributes.addFlashAttribute("mensaje", "Producto añadido al carrito correctamente.");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        } catch (ProductNotFoundException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: Producto no encontrado.");
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }

        // Redirigir de vuelta a la página del producto o al carrito
        return "redirect:/carrito";
    }

    /**
     * PUT /carrito/item/{id} - Actualizar cantidad
     * Requiere <input type="hidden" name="_method" value="put"/> en el formulario Pebble
     */
    @PutMapping("/item/{id}")
    public String updateItemQuantity(
            @PathVariable("id") Long productId, // Ojo: recibimos el ID del producto, no del item, según tu lógica de servicio
            @RequestParam Integer quantity,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User user = getAuthenticatedUser(principal);

        try {
            carritoService.updateQuantity(user, productId, quantity);
            redirectAttributes.addFlashAttribute("mensaje", "Cantidad actualizada.");
            redirectAttributes.addFlashAttribute("tipo", "info");
        } catch (InsufficientStockException e) {
            redirectAttributes.addFlashAttribute("mensaje", "No hay suficiente stock para esa cantidad.");
            redirectAttributes.addFlashAttribute("tipo", "warning");
        }

        return "redirect:/carrito";
    }

    /**
     * DELETE /carrito/item/{id} - Eliminar línea
     * Requiere <input type="hidden" name="_method" value="delete"/> en el formulario Pebble
     */
    @DeleteMapping("/item/{id}")
    public String removeFromCart(
            @PathVariable("id") Long productId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User user = getAuthenticatedUser(principal);
        carritoService.removeFromCart(user, productId);

        redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito.");
        redirectAttributes.addFlashAttribute("tipo", "info");

        return "redirect:/carrito";
    }

    /**
     * POST /carrito/checkout - Finalizar compra
     * Convierte el Carrito en un Pedido (Purchase)
     */
    @PostMapping("/checkout")
    public String checkout(Principal principal, RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(principal);

        try {
            // Aquí llamarías a tu servicio de compras:
            // Purchase pedido = purchaseService.processCheckout(user);

            // Simulamos el vaciado del carrito por ahora:
            carritoService.clearCart(user);

            redirectAttributes.addFlashAttribute("mensaje", "¡Compra realizada con éxito! Revisa tu email.");
            redirectAttributes.addFlashAttribute("tipo", "success");
            return "redirect:/user/pedidos"; // Redirigir al historial de pedidos

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al procesar la compra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
            return "redirect:/carrito";
        }
    }
}
