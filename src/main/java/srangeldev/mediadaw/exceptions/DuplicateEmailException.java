package srangeldev.mediadaw.exceptions;

/**
 * Excepción lanzada cuando ya existe un usuario con el mismo email.
 */
public class DuplicateEmailException extends MediaDawException {

    private final String email;

    public DuplicateEmailException(String email) {
        super(String.format("Ya existe un usuario registrado con el email '%s'", email), "DUPLICATE_EMAIL");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUserMessage() {
        return "Este email ya está registrado. ¿Has olvidado tu contraseña?";
    }
}
