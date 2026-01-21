package srangeldev.mediadaw.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Usuario de MediaDaw.
 * Representa tanto a clientes (USER) como a administradores (ADMIN).
 * Los clientes pueden ver productos, añadir al carrito y gestionar sus pedidos.
 * Los administradores gestionan el inventario de la tienda.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Debe estar encriptada con BCrypt

    @Column
    private String avatar;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaAlta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    // Relación: Un usuario tiene UN carrito activo
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Carrito carrito;

    // Relación: Un usuario realiza muchos pedidos
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private List<Venta> ventas = new ArrayList<>();
}
