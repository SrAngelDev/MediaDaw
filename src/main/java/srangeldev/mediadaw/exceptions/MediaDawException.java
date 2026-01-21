package srangeldev.mediadaw.exceptions;

/**
 * Excepción base para todas las excepciones de dominio.
 */
public abstract class MediaDawException extends RuntimeException {

    /**
     * Código de error específico para categorizar la excepción
     */
    private final String errorCode;

    /**
     * Constructor con mensaje
     *
     * @param message Mensaje descriptivo del error
     */
    protected MediaDawException(String message) {
        super(message);
        this.errorCode = generateErrorCode();
    }

    /**
     * Constructor con mensaje y causa
     *
     * @param message Mensaje descriptivo del error
     * @param cause Causa raíz de la excepción
     */
    protected MediaDawException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = generateErrorCode();
    }

    /**
     * Constructor con código de error personalizado
     *
     * @param message Mensaje descriptivo del error
     * @param errorCode Código de error específico
     */
    protected MediaDawException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor completo
     *
     * @param message Mensaje descriptivo del error
     * @param cause Causa raíz de la excepción
     * @param errorCode Código de error específico
     */
    protected MediaDawException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Genera un código de error basado en el nombre de la clase
     *
     * @return Código de error en formato UPPER_SNAKE_CASE
     */
    private String generateErrorCode() {
        String className = this.getClass().getSimpleName();
        // Convierte "ProductNotFoundException" a "PRODUCT_NOT_FOUND"
        return className.replaceAll("Exception$", "")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toUpperCase();
    }

    /**
     * Obtiene el código de error de esta excepción
     *
     * @return Código de error
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Obtiene un mensaje de error formateado para mostrar al usuario
     *
     * @return Mensaje de error user-friendly
     */
    public String getUserMessage() {
        return getMessage();
    }
}
