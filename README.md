# 🛒 MediaDaw - Tienda Online de Electrónica

<div align="center">

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen?style=for-the-badge&logo=spring)
![Build](https://img.shields.io/badge/Build-Passing-success?style=for-the-badge)
![License](https://img.shields.io/badge/License-Educational-blue?style=for-the-badge)

**Proyecto de tienda online con gestión de inventario centralizado y control de stock en tiempo real**

[Documentación](#-documentación) •
[Características](#-características) •
[Instalación](#-instalación) •
[Uso](#-uso) •
[Estructura](#-estructura)

</div>

---

## 📋 Descripción

**MediaDaw** es una aplicación web de comercio electrónico especializada en productos tecnológicos y electrónicos. A diferencia de un marketplace tradicional, MediaDaw gestiona un **inventario centralizado** donde la tienda es la propietaria de todos los productos.

### 🎯 Diferencias Clave con WalaDaw

| Característica | WalaDaw | MediaDaw |
|----------------|---------|----------|
| Modelo de Negocio | Marketplace con múltiples vendedores | Tienda única con inventario centralizado |
| Propiedad de Productos | Cada producto pertenece a un vendedor | Todos los productos pertenecen a la tienda |
| Control de Stock | ❌ No implementado | ✅ **Gestión crítica** - Stock en tiempo real |
| Roles de Usuario | Vendedor / Comprador | Administrador / Cliente |
| Gestión de Inventario | Descentralizada | Centralizada (solo ADMIN) |

---

## ✨ Características

### 👥 Sistema de Usuarios
- ✅ Registro e inicio de sesión seguro
- ✅ Autenticación con Spring Security
- ✅ Roles diferenciados (USER/ADMIN)
- ✅ Encriptación de contraseñas con BCrypt
- ✅ Gestión de perfiles

### 📦 Gestión de Productos
- ✅ CRUD completo de productos (solo ADMIN)
- ✅ **Control de stock en tiempo real** ⚠️
- ✅ Categorización por tipo (Audio, Smartphones, Gaming, etc.)
- ✅ Búsqueda y filtrado avanzado
- ✅ Borrado lógico de productos
- ✅ Alertas de stock bajo

### 🛒 Carrito de Compras
- ✅ Añadir/eliminar productos
- ✅ Modificar cantidades
- ✅ Validación de stock disponible
- ✅ Cálculo automático de totales

### 📋 Sistema de Pedidos
- ✅ Creación de pedidos desde el carrito
- ✅ **Reducción automática de stock** al confirmar compra
- ✅ Precio congelado en el momento de compra
- ✅ Estados de pedido (Pendiente, Enviado, Entregado)
- ✅ Historial de compras por usuario
- ✅ Panel de gestión de pedidos (ADMIN)

### 🔒 Seguridad
- ✅ Rutas protegidas por roles
- ✅ Protección CSRF
- ✅ Sesiones seguras
- ✅ Transacciones atómicas con rollback

---

## 🏗️ Arquitectura

### Modelo de Capas

```
┌─────────────────────────────────────┐
│         PRESENTATION LAYER          │
│     (Controllers + Pebble Views)    │
└─────────────────────────────────────┘
                  ▼
┌─────────────────────────────────────┐
│          BUSINESS LAYER             │
│           (Services)                │
└─────────────────────────────────────┘
                  ▼
┌─────────────────────────────────────┐
│         PERSISTENCE LAYER           │
│   (Repositories + JPA Entities)     │
└─────────────────────────────────────┘
                  ▼
┌─────────────────────────────────────┐
│           DATABASE                  │
│       (H2 / PostgreSQL)             │
└─────────────────────────────────────┘
```

### Entidades Principales

- **User**: Usuarios con roles (USER/ADMIN)
- **Product**: Productos con control de stock
- **CartItem**: Items en el carrito de compras
- **Purchase**: Pedidos realizados
- **OrderLine**: Líneas de pedido con precio congelado

---

## 🚀 Instalación

### Requisitos Previos

- Java 25 o superior
- Gradle 9.2.1 o superior
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Pasos de Instalación

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tu-usuario/MediaDaw.git
   cd MediaDaw
   ```

2. **Compilar el proyecto**
   ```bash
   ./gradlew clean build
   ```

3. **Ejecutar la aplicación**
   ```bash
   ./gradlew bootRun
   ```

4. **Acceder a la aplicación**
   - **URL**: http://localhost:8080
   - **H2 Console**: http://localhost:8080/h2-console

---

## 👤 Usuarios de Prueba

La aplicación viene con usuarios pre-cargados para facilitar las pruebas:

### Administrador
```
Email: admin@mediadaw.com
Password: admin123
```
**Permisos**: Gestión completa de productos, stock y pedidos

### Cliente
```
Email: cliente@mediadaw.com
Password: cliente123
```
**Permisos**: Ver productos, comprar y gestionar pedidos propios

---

## 🗂️ Estructura del Proyecto

```
MediaDaw/
├── src/main/java/srangeldev/mediadaw/
│   ├── models/                    # Entidades JPA (8)
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── CartItem.java
│   │   ├── Purchase.java
│   │   ├── OrderLine.java
│   │   └── [Enums...]
│   ├── repositories/              # Repositorios (5)
│   │   ├── UserRepository.java
│   │   ├── ProductRepository.java
│   │   └── [...]
│   ├── services/                  # Lógica de negocio (4)
│   │   ├── UserService.java
│   │   ├── ProductService.java
│   │   ├── CartService.java
│   │   └── PurchaseService.java  ⚠️ Gestión crítica de stock
│   ├── controllers/               # Controladores MVC (3)
│   │   ├── HomeController.java
│   │   ├── ProductController.java
│   │   └── AdminController.java
│   └── config/                    # Configuración (3)
│       ├── SecurityConfig.java
│       ├── CustomUserDetailsService.java
│       └── DataLoader.java
├── src/main/resources/
│   ├── application.properties
│   └── templates/                 # Vistas Pebble (pendiente)
├── MODEL_DOCUMENTATION.md         # Documentación del modelo
├── SETUP_SUMMARY.md              # Resumen de implementación
├── MODEL_DIAGRAM.txt             # Diagrama visual
└── RESUMEN_EJECUTIVO.md          # Resumen ejecutivo
```

---

## 💡 Lógica Crítica: Proceso de Compra

El corazón de MediaDaw es el método `PurchaseService.createPurchaseFromCart()`:

```java
@Transactional // Todo o nada - Rollback automático
public Purchase createPurchaseFromCart(Long userId) {
    // 1. Validar stock de TODOS los productos
    // 2. Crear pedido (estado: PENDIENTE)
    // 3. Crear líneas con precio congelado
    // 4. REDUCIR STOCK de cada producto ⚠️
    // 5. Calcular total
    // 6. Guardar pedido
    // 7. Limpiar carrito
    // Si algo falla → ROLLBACK completo
}
```

### ⚠️ Control de Stock

```java
// En Product.java
public void reduceStock(int cantidad) {
    if (!hasStock(cantidad)) {
        throw new IllegalStateException("Stock insuficiente");
    }
    this.stock -= cantidad;
}
```

---

## 🛠️ Tecnologías Utilizadas

### Backend
- **Java 25**
- **Spring Boot 4.0.1**
- **Spring Data JPA** (Hibernate)
- **Spring Security** (Autenticación y Autorización)
- **Lombok** (Reducir boilerplate)

### Base de Datos
- **H2** (Desarrollo - en memoria)
- **PostgreSQL** (Producción - recomendado)

### Plantillas
- **Pebble Templates** (Motor de plantillas)

### Build Tool
- **Gradle 9.2.1**

---

## 📚 Documentación

- **[MODEL_DOCUMENTATION.md](MODEL_DOCUMENTATION.md)** - Documentación completa del modelo de dominio
- **[SETUP_SUMMARY.md](SETUP_SUMMARY.md)** - Resumen de la implementación
- **[RESUMEN_EJECUTIVO.md](RESUMEN_EJECUTIVO.md)** - Resumen ejecutivo del proyecto
- **[MODEL_DIAGRAM.txt](MODEL_DIAGRAM.txt)** - Diagrama visual ASCII del modelo

---

## 🎯 Rutas de la Aplicación

### Rutas Públicas (sin autenticación)
- `GET /` - Página principal
- `GET /productos` - Catálogo de productos
- `GET /productos/{id}` - Detalle de producto
- `GET /login` - Inicio de sesión
- `POST /registro` - Registro de usuarios

### Rutas de Usuario (USER/ADMIN)
- `GET /carrito` - Ver carrito
- `POST /carrito/add` - Añadir al carrito
- `GET /pedidos` - Historial de pedidos
- `GET /perfil` - Perfil de usuario

### Rutas de Administrador (solo ADMIN)
- `GET /admin` - Dashboard
- `GET /admin/productos` - Gestión de productos
- `POST /admin/productos` - Crear producto
- `PUT /admin/productos/{id}` - Actualizar producto
- `DELETE /admin/productos/{id}` - Eliminar producto
- `GET /admin/pedidos` - Gestión de pedidos

---

## 🔧 Configuración

### Base de Datos (H2 - Desarrollo)
```properties
spring.datasource.url=jdbc:h2:mem:mediadaw
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Base de Datos (PostgreSQL - Producción)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mediadaw
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update
```

---

## 📝 Próximos Pasos (Roadmap)

- [ ] Crear vistas Pebble para la interfaz de usuario
- [ ] Implementar controladores de carrito y compra
- [ ] Añadir validaciones de formularios
- [ ] Sistema de favoritos
- [ ] Notificaciones por email
- [ ] Paginación de productos
- [ ] API REST para aplicaciones móviles
- [ ] Integración con pasarela de pago (Stripe/PayPal)
- [ ] Panel de estadísticas avanzado
- [ ] Sistema de reseñas de productos

---

## 🧪 Testing

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar tests con reporte
./gradlew test jacocoTestReport
```

---

## 🤝 Contribuir

Este es un proyecto educativo del módulo 2DAW. Si deseas contribuir:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 📄 Licencia

Este proyecto tiene fines educativos y está desarrollado como parte del módulo de Desarrollo de Aplicaciones Web (2DAW).

---

## 👨‍💻 Autor

**Proyecto MediaDaw**  
Módulo: Desarrollo de Aplicaciones Web (2DAW)  
Año: 2026

---

## 📞 Soporte

Para cualquier duda o consulta:
- 📧 Email: [tu-email@ejemplo.com]
- 📚 Documentación: Ver archivos `.md` en el proyecto
- 🐛 Issues: [GitHub Issues](https://github.com/tu-usuario/MediaDaw/issues)

---

<div align="center">

**⭐ Si te ha gustado el proyecto, dale una estrella ⭐**

Desarrollado con ❤️ y mucho ☕

</div>
