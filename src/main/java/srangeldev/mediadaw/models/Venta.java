package srangeldev.mediadaw.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Venta (Pedido).
 * Registra una transacción finalizada con su estado, fecha y total.
 */
@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCompra;

    @Column(nullable = false)
    private Double total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LineaVenta> lines = new ArrayList<>();

    /**
     * Añade una línea de pedido
     */
    public void addOrderLine(LineaVenta line) {
        lines.add(line);
        line.setVenta(this);
    }

    /**
     * Calcula el total del pedido basándose en las líneas
     */
    public Double calculateTotal() {
        return lines.stream()
                .mapToDouble(LineaVenta::getSubtotal)
                .sum();
    }
}
