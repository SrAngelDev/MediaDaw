package srangeldev.mediadaw.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import srangeldev.mediadaw.models.User;
import srangeldev.mediadaw.services.CarritoService;
import srangeldev.mediadaw.services.UserService;

/**
 *
 * Este componente expone automáticamente
 * variables globales a TODAS las vistas Pebble sin necesidad de pasarlas
 * manualmente en cada controlador.
 */
@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalControllerAdvice {

    private final UserService userService;
    private final CarritoService carritoService;

    @ModelAttribute("_csrf")
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        // Esto hace que {{ _csrf }} esté disponible en TODAS tus vistas Pebble
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }

    /**
     * Expone el usuario autenticado a todas las vistas.
     *
     * @return Usuario actual o null si no está autenticado
     */
    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {

                String email = authentication.getName();
                return userService.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            log.debug("No se pudo obtener el usuario actual: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Expone el número de items en el carrito a todas las vistas.
     *
     * Esto permite mostrar el contador en el header sin tener que
     * pasarlo explícitamente desde cada controlador.
     *
     * @return Número de items en el carrito (0 si no hay usuario autenticado)
     */
    @ModelAttribute("cartItemCount")
    public long getCartItemCount() {
        try {
            User currentUser = getCurrentUser();

            if (currentUser != null) {
                return carritoService.getCartItemCount(currentUser.getId());
            }
        } catch (Exception e) {
            log.debug("No se pudo obtener el contador del carrito: {}", e.getMessage());
        }

        return 0L;
    }

    /**
     * Expone el nombre de la aplicación a todas las vistas.
     *
     * @return Nombre de la aplicación
     */
    @ModelAttribute("appName")
    public String getAppName() {
        return "MediaDaw";
    }

    /**
     * Expone el año actual para el copyright en el footer.
     *
     * @return Año actual
     */
    @ModelAttribute("currentYear")
    public int getCurrentYear() {
        return java.time.Year.now().getValue();
    }

    /**
     * Verifica si el usuario actual tiene rol ADMIN.
     * Útil para mostrar/ocultar elementos en las vistas.
     *
     * @return true si el usuario es ADMIN
     */
    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        try {
            User currentUser = getCurrentUser();
            return currentUser != null && userService.isAdmin(currentUser.getId());
        } catch (Exception e) {
            log.debug("No se pudo verificar si el usuario es admin: {}", e.getMessage());
        }

        return false;
    }
}
