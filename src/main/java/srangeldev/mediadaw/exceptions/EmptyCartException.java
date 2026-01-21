package srangeldev.mediadaw.exceptions;

/**
 * Excepción lanzada cuando se intenta realizar una operación con un carrito vacío.
 */
public class EmptyCartException extends MediaDawException {

    private final Long userId;

    public EmptyCartException(Long userId) {
        super("No puedes realizar una compra con el carrito vacío", "EMPTY_CART");
        this.userId = userId;
    }

    public EmptyCartException(String message) {
        super(message, "EMPTY_CART");
        this.userId = null;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String getUserMessage() {
        return "Tu carrito está vacío. Añade productos antes de continuar con la compra.";
    }
}
