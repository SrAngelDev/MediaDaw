package srangeldev.mediadaw.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * Esencial para manejar cantidades con stock.
 * Guarda el precio del producto en el momento de la compra (precioVenta)
 * para mantener un histórico correcto aunque cambien los precios.
 */
@Entity
@Table(name = "order_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineaVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Double precioVenta; // Precio congelado al momento de la compra

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id")
    private Venta venta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Productos productos;

    /**
     * Calcula el subtotal de esta línea
     */
    public Double getSubtotal() {
        return precioVenta * cantidad;
    }
}
