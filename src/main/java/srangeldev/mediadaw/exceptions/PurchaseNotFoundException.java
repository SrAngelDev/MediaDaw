package srangeldev.mediadaw.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un pedido por su ID.
 */
public class PurchaseNotFoundException extends MediaDawException {

    private final Long purchaseId;

    public PurchaseNotFoundException(Long purchaseId) {
        super(String.format("Pedido con ID %d no encontrado", purchaseId));
        this.purchaseId = purchaseId;
    }

    public PurchaseNotFoundException(String message) {
        super(message);
        this.purchaseId = null;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    @Override
    public String getUserMessage() {
        return "No se encontró el pedido solicitado.";
    }
}
