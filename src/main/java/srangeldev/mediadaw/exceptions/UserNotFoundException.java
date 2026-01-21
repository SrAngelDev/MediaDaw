package srangeldev.mediadaw.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un usuario por su ID o email.
 */
public class UserNotFoundException extends MediaDawException {

    private final Object identifier; // Puede ser Long (id) o String (email)

    public UserNotFoundException(Long userId) {
        super(String.format("Usuario con ID %d no encontrado", userId));
        this.identifier = userId;
    }

    public UserNotFoundException(String email) {
        super(String.format("Usuario con email '%s' no encontrado", email));
        this.identifier = email;
    }

    public UserNotFoundException(String message, Object identifier) {
        super(message);
        this.identifier = identifier;
    }

    public Object getIdentifier() {
        return identifier;
    }

    @Override
    public String getUserMessage() {
        return "Usuario no encontrado. Por favor, verifica tus credenciales.";
    }
}
