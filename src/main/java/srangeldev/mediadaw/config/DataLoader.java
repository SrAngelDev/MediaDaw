package srangeldev.mediadaw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import srangeldev.mediadaw.models.*;
import srangeldev.mediadaw.repositories.ProductosRepository;
import srangeldev.mediadaw.repositories.UserRepository;
import srangeldev.mediadaw.repositories.VentaRepository;

/**
 * Carga datos de ejemplo en la base de datos al iniciar la aplicación.
 * Solo para desarrollo y pruebas.
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductosRepository productosRepository;
    private final VentaRepository ventaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Crear usuarios de ejemplo
        createUsers();

        // Crear productos de ejemplo
        createProducts();

        // Crear pedidos de ejemplo
        createOrders();

        System.out.println("✅ Datos de ejemplo cargados correctamente");
    }

    private void createUsers() {
        // Usuario administrador
        if (!userRepository.existsByEmail("admin@mediadaw.com")) {
            User admin = User.builder()
                    .nombre("Admin")
                    .apellidos("MediaDaw")
                    .email("admin@mediadaw.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .isDeleted(false) // Usuario activo
                    .build();
            userRepository.save(admin);
            System.out.println("👤 Admin creado - Email: admin@mediadaw.com | Password: admin123");
        }

        // Usuario cliente
        if (!userRepository.existsByEmail("cliente@mediadaw.com")) {
            User cliente = User.builder()
                    .nombre("Cliente")
                    .apellidos("Prueba")
                    .email("cliente@mediadaw.com")
                    .password(passwordEncoder.encode("cliente123"))
                    .role(Role.USER)
                    .isDeleted(false) // Usuario activo
                    .build();
            userRepository.save(cliente);
            System.out.println("👤 Cliente creado - Email: cliente@mediadaw.com | Password: cliente123");
        }
    }

    private void createProducts() {
        if (productosRepository.count() == 0) {
            // AUDIO
            productosRepository.save(Productos.builder()
                    .nombre("Auriculares Sony WH-1000XM5")
                    .descripcion("Auriculares inalámbricos con cancelación de ruido líder en la industria")
                    .precio(399.99)
                    .stock(25)
                    .category(Categoria.AUDIO)
                    .imagen("/images/productos/auriculares-sony.webp")
                    .build());

            productosRepository.save(Productos.builder()
                    .nombre("Altavoz JBL Flip 6")
                    .descripcion("Altavoz Bluetooth portátil resistente al agua")
                    .precio(129.99)
                    .stock(40)
                    .category(Categoria.AUDIO)
                    .imagen("/images/productos/altavoz-jbl.webp")
                    .build());

            // SMARTPHONES
            productosRepository.save(Productos.builder()
                    .nombre("iPhone 15 Pro")
                    .descripcion("Smartphone Apple con chip A17 Pro y cámara de 48MP")
                    .precio(1199.99)
                    .stock(15)
                    .category(Categoria.SMARTPHONES)
                    .imagen("/images/productos/iphone-15.webp")
                    .build());

            productosRepository.save(Productos.builder()
                    .nombre("Samsung Galaxy S24 Ultra")
                    .descripcion("Smartphone Android con S Pen y cámara de 200MP")
                    .precio(1099.99)
                    .stock(20)
                    .category(Categoria.SMARTPHONES)
                    .imagen("/images/productos/galaxy-s24.webp")
                    .build());

            // LAPTOPS
            productosRepository.save(Productos.builder()
                    .nombre("MacBook Pro 14\"")
                    .descripcion("Portátil profesional con chip M3 Pro y pantalla Liquid Retina XDR")
                    .precio(2499.99)
                    .stock(10)
                    .category(Categoria.LAPTOPS)
                    .imagen("/images/productos/macbook-pro.webp")
                    .build());

            productosRepository.save(Productos.builder()
                    .nombre("Dell XPS 15")
                    .descripcion("Portátil con Intel Core i7, 16GB RAM y pantalla 4K")
                    .precio(1899.99)
                    .stock(12)
                    .category(Categoria.LAPTOPS)
                    .imagen("/images/productos/dell-xps.webp")
                    .build());

            // GAMING
            productosRepository.save(Productos.builder()
                    .nombre("PlayStation 5")
                    .descripcion("Consola de videojuegos de nueva generación con SSD ultra rápido")
                    .precio(549.99)
                    .stock(8)
                    .category(Categoria.GAMING)
                    .imagen("/images/productos/ps5.webp")
                    .build());

            productosRepository.save(Productos.builder()
                    .nombre("Logitech G Pro X Superlight")
                    .descripcion("Ratón gaming inalámbrico profesional ultra ligero")
                    .precio(149.99)
                    .stock(30)
                    .category(Categoria.GAMING)
                    .imagen("/images/productos/logitech-gpro.webp")
                    .build());

            // IMAGEN
            productosRepository.save(Productos.builder()
                    .nombre("Canon EOS R6 Mark II")
                    .descripcion("Cámara mirrorless full frame de 24.2MP con vídeo 4K")
                    .precio(2499.99)
                    .stock(6)
                    .category(Categoria.IMAGEN)
                    .imagen("/images/productos/canon-r6.webp")
                    .build());

            productosRepository.save(Productos.builder()
                    .nombre("GoPro Hero 12 Black")
                    .descripcion("Cámara de acción 5.3K con estabilización HyperSmooth")
                    .precio(449.99)
                    .stock(18)
                    .category(Categoria.IMAGEN)
                    .imagen("/images/productos/gopro-12.webp")
                    .build());

            // INSTRUMENTOS
            productosRepository.save(Productos.builder()
                    .nombre("Yamaha P-125 Digital Piano")
                    .descripcion("Piano digital de 88 teclas con sonido GHS")
                    .precio(699.99)
                    .stock(5)
                    .category(Categoria.INSTRUMENTOS)
                    .imagen("/images/productos/yamaha-piano.webp")
                    .build());

            productosRepository.save(Productos.builder()
                    .nombre("Fender Player Stratocaster")
                    .descripcion("Guitarra eléctrica clásica hecha en México")
                    .precio(849.99)
                    .stock(7)
                    .category(Categoria.INSTRUMENTOS)
                    .imagen("/images/productos/fender-strat.webp")
                    .build());

            System.out.println("📦 " + productosRepository.count() + " productos de ejemplo creados");
        }
    }

    private void createOrders() {
        if (ventaRepository.count() == 0) {
            User cliente = userRepository.findByEmail("cliente@mediadaw.com").orElse(null);
            User admin = userRepository.findByEmail("admin@mediadaw.com").orElse(null);

            if (cliente == null || admin == null) {
                System.out.println("⚠️ No se pudieron crear pedidos: usuarios no encontrados");
                return;
            }

            // Obtener algunos productos
            Productos iphone = productosRepository.findById(3L).orElse(null);
            Productos jbl = productosRepository.findById(2L).orElse(null);
            Productos ps5 = productosRepository.findById(7L).orElse(null);
            Productos macbook = productosRepository.findById(5L).orElse(null);
            Productos gopro = productosRepository.findById(10L).orElse(null);

            // Pedido 1: Cliente - PENDIENTE
            if (iphone != null && jbl != null) {
                Venta pedido1 = Venta.builder()
                        .user(cliente)
                        .estado(EstadoPedido.PENDIENTE)
                        .build();

                LineaVenta linea1 = LineaVenta.builder()
                        .productos(iphone)
                        .cantidad(1)
                        .precioVenta(iphone.getPrecio())
                        .venta(pedido1)
                        .build();

                LineaVenta linea2 = LineaVenta.builder()
                        .productos(jbl)
                        .cantidad(2)
                        .precioVenta(jbl.getPrecio())
                        .venta(pedido1)
                        .build();

                pedido1.addOrderLine(linea1);
                pedido1.addOrderLine(linea2);
                pedido1.setTotal(pedido1.calculateTotal());

                ventaRepository.save(pedido1);
            }

            // Pedido 2: Cliente - ENVIADO
            if (ps5 != null) {
                Venta pedido2 = Venta.builder()
                        .user(cliente)
                        .estado(EstadoPedido.ENVIADO)
                        .build();

                LineaVenta linea1 = LineaVenta.builder()
                        .productos(ps5)
                        .cantidad(1)
                        .precioVenta(ps5.getPrecio())
                        .venta(pedido2)
                        .build();

                pedido2.addOrderLine(linea1);
                pedido2.setTotal(pedido2.calculateTotal());

                ventaRepository.save(pedido2);
            }

            // Pedido 3: Admin - ENTREGADO
            if (macbook != null && gopro != null) {
                Venta pedido3 = Venta.builder()
                        .user(admin)
                        .estado(EstadoPedido.ENTREGADO)
                        .build();

                LineaVenta linea1 = LineaVenta.builder()
                        .productos(macbook)
                        .cantidad(1)
                        .precioVenta(macbook.getPrecio())
                        .venta(pedido3)
                        .build();

                LineaVenta linea2 = LineaVenta.builder()
                        .productos(gopro)
                        .cantidad(1)
                        .precioVenta(gopro.getPrecio())
                        .venta(pedido3)
                        .build();

                pedido3.addOrderLine(linea1);
                pedido3.addOrderLine(linea2);
                pedido3.setTotal(pedido3.calculateTotal());

                ventaRepository.save(pedido3);
            }

            // Pedido 4: Cliente - ENTREGADO
            if (jbl != null) {
                Venta pedido4 = Venta.builder()
                        .user(cliente)
                        .estado(EstadoPedido.ENTREGADO)
                        .build();

                LineaVenta linea1 = LineaVenta.builder()
                        .productos(jbl)
                        .cantidad(3)
                        .precioVenta(jbl.getPrecio())
                        .venta(pedido4)
                        .build();

                pedido4.addOrderLine(linea1);
                pedido4.setTotal(pedido4.calculateTotal());

                ventaRepository.save(pedido4);
            }

            System.out.println("🛒 " + ventaRepository.count() + " pedidos de ejemplo creados");
        }
    }
}
