package srangeldev.mediadaw.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta comprar más unidades de un producto
 * de las que hay disponibles en stock.
 */
public class InsufficientStockException extends MediaDawException {

    private final Long productId;
    private final String productName;
    private final int requestedQuantity;
    private final int availableStock;

    /**
     * Constructor completo con toda la información del stock
     *
     * @param productId ID del producto
     * @param productName Nombre del producto
     * @param requestedQuantity Cantidad solicitada
     * @param availableStock Stock disponible actual
     */
    public InsufficientStockException(Long productId, String productName,
                                     int requestedQuantity, int availableStock) {
        super(String.format(
            "Stock insuficiente para '%s'. Solicitado: %d, Disponible: %d",
            productName, requestedQuantity, availableStock
        ), "INSUFFICIENT_STOCK");

        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    /**
     * Constructor simplificado con mensaje personalizado
     *
     * @param productName Nombre del producto
     * @param availableStock Stock disponible
     */
    public InsufficientStockException(String productName, int availableStock) {
        super(String.format(
            "Stock insuficiente para '%s'. Solo quedan %d unidades disponibles",
            productName, availableStock
        ), "INSUFFICIENT_STOCK");

        this.productId = null;
        this.productName = productName;
        this.requestedQuantity = 0;
        this.availableStock = availableStock;
    }

    /**
     * Constructor con mensaje genérico
     *
     * @param message Mensaje descriptivo
     */
    public InsufficientStockException(String message) {
        super(message, "INSUFFICIENT_STOCK");
        this.productId = null;
        this.productName = null;
        this.requestedQuantity = 0;
        this.availableStock = 0;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    @Override
    public String getUserMessage() {
        if (productName != null && availableStock > 0) {
            return String.format(
                "Lo sentimos, solo tenemos %d unidad(es) disponible(s) de '%s' en este momento.",
                availableStock, productName
            );
        } else if (productName != null) {
            return String.format(
                "Lo sentimos, el producto '%s' está agotado en este momento.",
                productName
            );
        }
        return "No hay suficiente stock disponible para completar tu pedido.";
    }

    /**
     * Verifica si el producto está completamente agotado
     *
     * @return true si no hay stock disponible
     */
    public boolean isOutOfStock() {
        return availableStock <= 0;
    }
}
