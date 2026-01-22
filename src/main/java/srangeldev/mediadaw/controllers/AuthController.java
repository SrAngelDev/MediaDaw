package srangeldev.mediadaw.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import srangeldev.mediadaw.models.User;
import srangeldev.mediadaw.services.UserService;
import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * GET /login - Muestra el formulario de acceso.
     * Spring Security redirige aquí si no estás autenticado.
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos.");
        }

        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión correctamente.");
        }

        return "auth/login";
    }

    /**
     * GET /registro - Muestra el formulario de registro.
     */
    @GetMapping("/registro")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    /**
     * POST /registro - Procesa la creación del usuario.
     */
    @PostMapping("/registro")
    public String registerProcess(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 1. Validaciones básicas de campos
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // 2. Validación de coincidencia de contraseñas
        if (!user.getPassword().equals(confirmPassword)) {
            bindingResult.rejectValue("password", "error.user", "Las contraseñas no coinciden");
            return "auth/register";
        }

        // 3. Intentar guardar
        try {
            userService.registerUser(user);

            // Usamos Flash Attributes para que el mensaje sobreviva a la redirección
            redirectAttributes.addFlashAttribute("mensaje", "¡Registro exitoso! Por favor inicia sesión.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            // Error de negocio (ej. email duplicado)
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}
