package srangeldev.mediadaw.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un producto por su ID.
 *
 * Esta excepción se utiliza en la capa de servicio cuando se solicita
 * un producto que no existe en la base de datos o que ha sido eliminado
 * lógicamente (deleted = true).
 *
 * Ejemplo de uso:
 * <pre>
 * Productos productos = productRepository.findById(id)
 *     .orElseThrow(() -> new ProductNotFoundException(id));
 * </pre>
 *
 * @author MediaDaw Team
 */
public class ProductNotFoundException extends MediaDawException {

    private final Long productId;

    /**
     * Constructor con ID del producto
     *
     * @param productId ID del producto no encontrado
     */
    public ProductNotFoundException(Long productId) {
        super(String.format("Producto con ID %d no encontrado", productId));
        this.productId = productId;
    }

    /**
     * Constructor con ID y mensaje personalizado
     *
     * @param productId ID del producto no encontrado
     * @param message Mensaje personalizado
     */
    public ProductNotFoundException(Long productId, String message) {
        super(message);
        this.productId = productId;
    }

    /**
     * Constructor con mensaje simple (sin ID)
     *
     * @param message Mensaje descriptivo
     */
    public ProductNotFoundException(String message) {
        super(message);
        this.productId = null;
    }

    /**
     * Obtiene el ID del producto no encontrado
     *
     * @return ID del producto o null si no se especificó
     */
    public Long getProductId() {
        return productId;
    }

    @Override
    public String getUserMessage() {
        if (productId != null) {
            return "Lo sentimos, el producto solicitado no está disponible en este momento.";
        }
        return "El producto que buscas no existe o ya no está disponible.";
    }
}
