package srangeldev.mediadaw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import srangeldev.mediadaw.models.Categoria;
import srangeldev.mediadaw.services.ProductosService;

/**
 * Controlador para la página principal.
 *
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductosService productosService;

    /**
     * Página de inicio que muestra productos destacados.
     *
     */
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Obtener productos destacados (limitados a 8 para la home)
        var products = productosService.getAvailableProducts()
                .stream()
                .limit(8)
                .toList();

        model.addAttribute("products", products);
        model.addAttribute("categories", Categoria.values());

        return "index";
    }
}
