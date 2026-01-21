package srangeldev.mediadaw.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta realizar una acción que requiere
 * privilegios de administrador (ADMIN) sin tenerlos.
 */
public class UnauthorizedAccessException extends MediaDawException {

    private final String action;
    private final Long userId;
    private final String requiredRole;

    /**
     * Constructor completo con acción, usuario y rol requerido
     *
     * @param action Acción que se intentó realizar
     * @param userId ID del usuario que intentó la acción
     * @param requiredRole Rol necesario para la acción
     */
    public UnauthorizedAccessException(String action, Long userId, String requiredRole) {
        super(String.format(
            "Usuario %d no autorizado para '%s'. Se requiere rol: %s",
            userId, action, requiredRole
        ), "UNAUTHORIZED_ACCESS");

        this.action = action;
        this.userId = userId;
        this.requiredRole = requiredRole;
    }

    /**
     * Constructor con mensaje personalizado
     *
     * @param message Mensaje descriptivo
     */
    public UnauthorizedAccessException(String message) {
        super(message, "UNAUTHORIZED_ACCESS");
        this.action = null;
        this.userId = null;
        this.requiredRole = null;
    }

    /**
     * Constructor con acción y rol requerido
     *
     * @param action Acción que se intentó realizar
     * @param requiredRole Rol necesario
     */
    public UnauthorizedAccessException(String action, String requiredRole) {
        super(String.format(
            "No autorizado para '%s'. Se requiere rol: %s",
            action, requiredRole
        ), "UNAUTHORIZED_ACCESS");

        this.action = action;
        this.userId = null;
        this.requiredRole = requiredRole;
    }

    public String getAction() {
        return action;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    @Override
    public String getUserMessage() {
        if (action != null) {
            return String.format(
                "No tienes permisos para realizar esta acción: %s. Contacta con un administrador.",
                action
            );
        }
        return "No tienes permisos suficientes para acceder a este recurso.";
    }
}
