package srangeldev.mediadaw.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad Producto de MediaDaw.
 * Es el corazón de la tienda. Diferencia clave con WalaDaw:
 * los productos pertenecen al inventario de la tienda, NO a usuarios vendedores.
 * El campo stock es vital para gestionar la disponibilidad.
 */
@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Productos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Column
    private String imagen;

    @Column(nullable = false)
    private Integer stock; // Stock disponible en la tienda

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria category;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaAlta;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false; // Borrado lógico

    /**
     * Verifica si hay stock suficiente disponible
     */
    public boolean hasStock(int quantity) {
        return !deleted && stock >= quantity;
    }

    /**
     * Reduce el stock del producto
     * @param quantity cantidad a reducir
     * @throws IllegalStateException si no hay stock suficiente
     */
    public void reduceStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + nombre);
        }
        this.stock -= quantity;
    }

    /**
     * Aumenta el stock del producto (para devoluciones o reposición)
     */
    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
}
