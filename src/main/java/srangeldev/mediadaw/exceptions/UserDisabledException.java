package srangeldev.mediadaw.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta acceder al sistema
 * pero su cuenta ha sido deshabilitada (deleted = true).
 */
public class UserDisabledException extends MediaDawException {

    private final Long userId;
    private final String email;

    /**
     * Constructor con ID de usuario
     *
     * @param userId ID del usuario deshabilitado
     */
    public UserDisabledException(Long userId) {
        super(String.format("La cuenta de usuario con ID %d ha sido deshabilitada", userId),
              "USER_DISABLED");
        this.userId = userId;
        this.email = null;
    }

    /**
     * Constructor con email
     *
     * @param email Email del usuario deshabilitado
     */
    public UserDisabledException(String email) {
        super(String.format("La cuenta con email '%s' ha sido deshabilitada", email),
              "USER_DISABLED");
        this.userId = null;
        this.email = email;
    }

    /**
     * Constructor completo
     *
     * @param userId ID del usuario
     * @param email Email del usuario
     */
    public UserDisabledException(Long userId, String email) {
        super(String.format("La cuenta de %s ha sido deshabilitada", email),
              "USER_DISABLED");
        this.userId = userId;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUserMessage() {
        return "Tu cuenta ha sido deshabilitada. Por favor, contacta con el administrador para más información.";
    }
}
