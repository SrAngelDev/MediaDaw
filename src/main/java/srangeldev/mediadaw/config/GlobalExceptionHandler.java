package srangeldev.mediadaw.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import srangeldev.mediadaw.exceptions.*;

/**
 * Manejador global de excepciones.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleProductNotFound(
            ProductNotFoundException ex,
            RedirectAttributes redirectAttributes) {

        log.warn("Producto no encontrado: {}", ex.getMessage());

        redirectAttributes.addFlashAttribute("errorType", "warning");
        redirectAttributes.addFlashAttribute("errorMessage", ex.getUserMessage());
        redirectAttributes.addFlashAttribute("errorCode", ex.getErrorCode());

        return "redirect:/productos";
    }

    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleInsufficientStock(
            InsufficientStockException ex,
            RedirectAttributes redirectAttributes) {

        log.error("Stock insuficiente: {} - Solicitado: {}, Disponible: {}",
                ex.getProductName(),
                ex.getRequestedQuantity(),
                ex.getAvailableStock());

        redirectAttributes.addFlashAttribute("errorType", "danger");
        redirectAttributes.addFlashAttribute("errorMessage", ex.getUserMessage());
        redirectAttributes.addFlashAttribute("errorCode", ex.getErrorCode());

        // Información adicional para debugging en desarrollo
        if (ex.getProductId() != null) {
            redirectAttributes.addFlashAttribute("productId", ex.getProductId());
            redirectAttributes.addFlashAttribute("availableStock", ex.getAvailableStock());
        }

        return "redirect:/carrito";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Resource> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        String requestedPath = request.getRequestURI();

        // Solo manejar imágenes de productos
        if (requestedPath != null && requestedPath.contains("/images/productos/")) {
            log.warn("Imagen de producto no encontrada: {} - Devolviendo imagen por defecto", requestedPath);

            try {
                // Devolver imagen por defecto de productos
                Resource defaultImage = new ClassPathResource("static/images/productos/default-product.svg");

                if (defaultImage.exists()) {
                    return ResponseEntity
                            .ok()
                            .contentType(MediaType.valueOf("image/svg+xml"))
                            .body(defaultImage);
                }
            } catch (Exception e) {
                log.error("Error al cargar imagen por defecto: {}", e.getMessage());
            }
        } else if (requestedPath != null && requestedPath.contains("/images/")) {
            // Para otras imágenes, intentar devolver el logo
            log.warn("Recurso de imagen no encontrado: {} - Devolviendo logo", requestedPath);

            try {
                Resource defaultImage = new ClassPathResource("static/images/logo.png");

                if (defaultImage.exists()) {
                    return ResponseEntity
                            .ok()
                            .contentType(MediaType.IMAGE_PNG)
                            .body(defaultImage);
                }
            } catch (Exception e) {
                log.error("Error al cargar logo: {}", e.getMessage());
            }
        }

        // Si no es una imagen o falla la carga, devolver 404
        log.warn("Recurso no encontrado: {}", requestedPath);
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(UserDisabledException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUserDisabled(
            UserDisabledException ex,
            RedirectAttributes redirectAttributes) {

        log.warn("Intento de acceso con usuario deshabilitado: {}", ex.getEmail());

        redirectAttributes.addFlashAttribute("errorType", "danger");
        redirectAttributes.addFlashAttribute("errorMessage", ex.getUserMessage());
        redirectAttributes.addFlashAttribute("errorCode", ex.getErrorCode());

        return "redirect:/login";
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFound(
            UserNotFoundException ex,
            RedirectAttributes redirectAttributes) {

        log.warn("Usuario no encontrado: {}", ex.getMessage());

        redirectAttributes.addFlashAttribute("errorType", "warning");
        redirectAttributes.addFlashAttribute("errorMessage", ex.getUserMessage());

        return "redirect:/login";
    }

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateEmail(
            DuplicateEmailException ex,
            RedirectAttributes redirectAttributes) {

        log.warn("Intento de registro con email duplicado: {}", ex.getEmail());

        redirectAttributes.addFlashAttribute("errorType", "warning");
        redirectAttributes.addFlashAttribute("errorMessage", ex.getUserMessage());
        redirectAttributes.addFlashAttribute("email", ex.getEmail());

        return "redirect:/registro";
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorizedAccess(
            UnauthorizedAccessException ex,
            RedirectAttributes redirectAttributes) {

        log.error("Acceso no autorizado: {}", ex.getMessage());

        redirectAttributes.addFlashAttribute("errorType", "danger");
        redirectAttributes.addFlashAttribute("errorMessage", ex.getUserMessage());
        redirectAttributes.addFlashAttribute("errorCode", ex.getErrorCode());

        return "redirect:/";
    }

    @ExceptionHandler(EmptyCartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleEmptyCart(
            EmptyCartException ex,
            RedirectAttributes redirectAttributes) {

        log.info("Intento de compra con carrito vacío - Usuario: {}", ex.getUserId());

        redirectAttributes.addFlashAttribute("errorType", "info");
        redirectAttributes.addFlashAttribute("errorMessage", ex.getUserMessage());

        return "redirect:/carrito";
    }

    @ExceptionHandler(PurchaseNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handlePurchaseNotFound(
            PurchaseNotFoundException ex,
            RedirectAttributes redirectAttributes) {

        log.warn("Pedido no encontrado: {}", ex.getMessage());

        redirectAttributes.addFlashAttribute("errorType", "warning");
        redirectAttributes.addFlashAttribute("errorMessage", ex.getUserMessage());

        return "redirect:/pedidos";
    }

    @ExceptionHandler(MediaDawException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleMediaDawException(
            MediaDawException ex,
            Model model) {

        log.error("Error de dominio MediaDaw: {}", ex.getMessage(), ex);

        model.addAttribute("errorType", "danger");
        model.addAttribute("errorMessage", ex.getUserMessage());
        model.addAttribute("errorCode", ex.getErrorCode());
        model.addAttribute("errorDetails", ex.getMessage());

        return "error";
    }

    /*
    * Con esto manejo cualquier otra excepción no contemplada
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(
            Exception ex,
            Model model) {

        log.error("Error inesperado: {}", ex.getMessage(), ex);

        model.addAttribute("errorType", "danger");
        model.addAttribute("errorMessage", "Ha ocurrido un error inesperado. Por favor, inténtalo de nuevo más tarde.");
        model.addAttribute("errorCode", "INTERNAL_ERROR");

        // En desarrollo, mostrar detalles técnicos
        // En producción, comentar esta línea
        model.addAttribute("errorDetails", ex.getMessage());

        return "error";
    }
}
