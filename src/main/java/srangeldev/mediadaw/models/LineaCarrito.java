package srangeldev.mediadaw.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * La entidad LineaCarrito que representa un ítem en el carrito de compras.
 * Persiste los productos que el usuario desea comprar antes de formalizar el pedido.
 */
@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineaCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private Integer cantidad = 1;

    // Relación N:1 con Carrito (La línea pertenece a un carrito)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Productos productos;

    /**
     * Calcula el subtotal de este item del carrito
     */
    public Double getSubtotal() {
        return productos.getPrecio() * cantidad;
    }

    /**
     * Incrementa la cantidad del item
     */
    public void incrementQuantity(int amount) {
        this.cantidad += amount;
    }
}
