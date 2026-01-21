package srangeldev.mediadaw.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carts")

public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación 1:1 con Usuario (Un usuario tiene un carrito activo)
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;


    // Relación 1:N con las líneas del carrito (Un carrito tiene muchas líneas)
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<LineaCarrito> lineaCarritos = new ArrayList<>();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Helper para recalcular el total dinámicamente
     */
    public Double getTotal() {
        return lineaCarritos.stream()
                .mapToDouble(LineaCarrito::getSubtotal)
                .sum();
    }

    /**
     * Helper para contar productos (suma de cantidades)
     */
    public Integer getTotalItems() {
        return lineaCarritos.stream()
                .mapToInt(LineaCarrito::getCantidad)
                .sum();
    }

    // Método helper para añadir líneas sincronizando la relación bidireccional
    public void addLineaCarrito(LineaCarrito item) {
        lineaCarritos.add(item);
        item.setCarrito(this);
    }

    public void removeLineaCarrito(LineaCarrito item) {
        lineaCarritos.remove(item);
        item.setCarrito(null);
    }
}