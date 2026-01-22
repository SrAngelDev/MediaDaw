# 🛒 MediaDaw - Tienda Online

<div align="center">

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen?style=for-the-badge&logo=spring)
![Build](https://img.shields.io/badge/Build-Passing-success?style=for-the-badge)
![License](https://img.shields.io/badge/License-Educational-blue?style=for-the-badge)

**Proyecto de tienda online con gestión de inventario centralizado y control de stock en tiempo real**

</div>

---

## 📋 Descripción

**MediaDaw** es una aplicación web de comercio electrónico especializada en todo tipo de productos de diferentes categorias. A diferencia de un marketplace tradicional, MediaDaw gestiona un **inventario centralizado** donde la tienda es la propietaria de todos los productos, con control estricto de stock en tiempo real.

### 💾 Arquitectura de Datos

**MediaDaw utiliza exclusivamente:**
- ✅ **Spring Data JPA** con Hibernate (ORM)
- ✅ **Bases de datos relacionales** (H2, MySQL, MariaDB)
- ✅ **Relaciones bidireccionales** (@OneToOne, @OneToMany, @ManyToOne)
- ✅ **Transacciones ACID** para garantizar consistencia

---

## ✨ Características Implementadas

### 👥 Sistema de Usuarios
- ✅ Registro e inicio de sesión seguro
- ✅ Autenticación con Spring Security
- ✅ Roles diferenciados (USER/ADMIN)
- ✅ Encriptación de contraseñas con BCrypt
- ✅ Gestión de perfiles
- ✅ Borrado lógico de usuarios (mantiene auditoría)

### 📦 Gestión de Productos
- ✅ CRUD completo de productos (solo ADMIN)
- ✅ **Control de stock en tiempo real** ⚠️
- ✅ Categorización por tipo (Audio, Smartphones, Gaming, etc.)
- ✅ Búsqueda y filtrado avanzado
- ✅ Borrado lógico de productos
- ✅ Alertas de stock bajo
- ✅ Vistas responsive con tarjetas de producto

### 🛒 Carrito de Compras (Arquitectura Robusta)
- ✅ **Arquitectura User → Carrito → LineaCarrito** (similar a Venta-LineaVenta)
- ✅ Lazy creation: carrito creado automáticamente al primer uso
- ✅ Añadir/eliminar productos
- ✅ Modificar cantidades con validación de stock
- ✅ **Validación estricta de stock antes de cada operación** ⚠️
- ✅ Cálculo automático de totales
- ✅ Relaciones bidireccionales correctamente sincronizadas
- ✅ Transaccionalidad completa con rollback

### 📋 Sistema de Pedidos (Ventas)
- ✅ Creación de pedidos desde el carrito
- ✅ **Reducción automática de stock** al confirmar compra
- ✅ Precio congelado en el momento de compra (evita cambios retrospectivos)
- ✅ Estados de pedido (PENDIENTE, ENVIADO, ENTREGADO, CANCELADO)
- ✅ Historial de compras por usuario
- ✅ Panel de gestión de pedidos (ADMIN)
- ✅ Restauración de stock al cancelar pedidos

### 🎨 Sistema de Vistas (Pebble Templates)
- ✅ Layout base modular con header y footer
- ✅ Componentes reutilizables (productCard, alert)
- ✅ Vista de homepage con productos destacados
- ✅ Vista de listado de productos con filtros
- ✅ Vista de detalle de producto completo
- ✅ GlobalControllerAdvice (variables automáticas: currentUser, cartItemCount)
- ✅ Sistema de mensajes flash
- ✅ Diseño responsive con Bootstrap 5
- ✅ Estilos de marca MediaDaw (rojo #CC0000, amarillo #FFCC00)

### 🔒 Seguridad
- ✅ Rutas protegidas por roles
- ✅ Protección CSRF en todos los formularios
- ✅ Sesiones seguras con HttpOnly cookies
- ✅ Transacciones atómicas con rollback
- ✅ CustomUserDetailsService para autenticación
- ✅ Manejo centralizado de excepciones (GlobalExceptionHandler)

---

## 🏗️ Arquitectura

### Modelo de Capas

```
┌─────────────────────────────────────┐
│         PRESENTATION LAYER          │
│     (Controllers + Pebble Views)    │
│   - HomeController                  │
│   - ProductController               │
│   - AdminController                 │
│   + GlobalControllerAdvice          │
└─────────────────────────────────────┘
                  ▼
┌─────────────────────────────────────┐
│          BUSINESS LAYER             │
│           (Services)                │
│   - UserService                     │
│   - ProductosService                │
│   - CarritoService                  │
│   - VentaService                    │
└─────────────────────────────────────┘
                  ▼
┌─────────────────────────────────────┐
│         PERSISTENCE LAYER           │
│   (Repositories + JPA Entities)     │
│   - UserRepository                  │
│   - ProductosRepository             │
│   - CarritoRepository               │
│   - LineaCarritoRepository          │
│   - VentaRepository                 │
│   - LineaVentaRepository            │
└─────────────────────────────────────┘
                  ▼
┌─────────────────────────────────────┐
│           DATABASE                  │
│     (H2 / MySQL / MariaDB)          │
│   + JPA/Hibernate (ORM)             │
│   + DDL auto-generated              │
└─────────────────────────────────────┘
```

### Diagrama de Entidades (Modelo de Dominio)

```
┌─────────────┐
│    User     │
│─────────────│
│ id          │
│ nombre      │
│ email       │◄──────────┐
│ password    │           │ 1:1
│ role        │           │
└─────────────┘           │
      │ 1:N               │
      │                   │
      ▼                   │
┌─────────────┐    ┌──────┴──────┐
│    Venta    │    │   Carrito   │
│─────────────│    │─────────────│
│ id          │    │ id          │
│ fechaCompra │    │ user        │
│ total       │    │ updatedAt   │
│ estado      │    └─────────────┘
│ user        │           │ 1:N
└─────────────┘           │
      │ 1:N               ▼
      │            ┌──────────────┐
      │            │ LineaCarrito │
      │            │──────────────│
      │            │ id           │
      │            │ carrito      │
      │            │ productos    │
      │            │ cantidad     │
      ▼            └──────────────┘
┌─────────────┐           │ N:1
│ LineaVenta  │           │
│─────────────│           │
│ id          │           │
│ venta       │           │
│ productos   │◄──────────┘
│ cantidad    │           
│ precioVenta │◄──────────────────┐
└─────────────┘                   │
                                  │ N:1
                           ┌──────┴──────┐
                           │  Productos  │
                           │─────────────│
                           │ id          │
                           │ nombre      │
                           │ precio      │
                           │ stock       │
                           │ category    │
                           │ deleted     │
                           └─────────────┘
```

### Relaciones Clave

1. **User → Carrito** (1:1): Un usuario tiene un carrito activo
2. **Carrito → LineaCarrito** (1:N): Un carrito contiene muchas líneas
3. **LineaCarrito → Productos** (N:1): Cada línea referencia un producto
4. **User → Venta** (1:N): Un usuario puede tener múltiples pedidos
5. **Venta → LineaVenta** (1:N): Un pedido tiene múltiples líneas
6. **LineaVenta → Productos** (N:1): Cada línea referencia un producto

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
│   ├── models/                        # Entidades JPA (9)
│   │   ├── User.java                  # Usuario con roles
│   │   ├── Productos.java             # Productos con stock
│   │   ├── Carrito.java               # Carrito de compras
│   │   ├── LineaCarrito.java          # Líneas del carrito
│   │   ├── Venta.java                 # Pedidos finalizados
│   │   ├── LineaVenta.java            # Líneas de pedido
│   │   ├── Role.java                  # Enum roles
│   │   ├── Categoria.java             # Enum categorías
│   │   └── EstadoPedido.java          # Enum estados pedido
│   │
│   ├── repositories/                  # Repositorios JPA
│   │   ├── UserRepository.java
│   │   ├── ProductosRepository.java
│   │   ├── CarritoRepository.java
│   │   ├── LineaCarritoRepository.java
│   │   ├── VentaRepository.java
│   │   └── LineaVentaRepository.java
│   │
│   ├── services/                      # Lógica de negocio
│   │   ├── UserService.java
│   │   ├── ProductService.java
│   │   ├── CarritoService.java        # Gestión del carrito
│   │   └── PurchaseService.java       # Gestión crítica de stock
│   │
│   ├── controllers/                   # Controladores 
│   │   ├── HomeController.java
│   │   ├── ProductController.java
│   │   ├── CartController.java
│   │   ├── AuthController.java
│   │   └── AdminController.java
│   │   
│   │
│   ├── security/                      # Configuración de seguridad
│   │   └── CustomUserDetailsService.java
│   │
│   ├── config/                        # Configuración Spring
│   │   ├── SecurityConfig.java
│   │   ├── GlobalControllerAdvice.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── DataLoader.java
│   │
│   └── exceptions/                    # Excepciones personalizadas
│       ├── MediaDawException.java
│       ├── UserNotFoundException.java
│       ├── ProductNotFoundException.java
│       ├── InsufficientStockException.java
│       ├── EmptyCartException.java
│       └── PurchaseNotFoundException.java
│
├── src/main/resources/
│   ├── application.properties         # Configuración de la app
│   ├── static/
│   │   ├── css/
│   │   │   └── mediadaw.css          # Estilos personalizados
│   │   └── images/
│   │       ├── logo.png
│   │       └── productos/
│   │
│   └── templates/                     # Vistas Pebble
│       ├── layouts/
│       │   └── base.peb              # Layout base
│       ├── fragments/
│       │   ├── header.peb            # Header reutilizable
│       │   ├── footer.peb            # Footer reutilizable
│       │   └── messages.peb          # Mensajes flash
│       ├── components/
│       │   ├── productCard.peb       # Tarjeta de producto
│       │   └── alert.peb             # Componente alerta
│       ├── productos/
│       │   ├── list.peb              # Listado de productos
│       │   └── detalleProducto.peb   # Detalle completo
│       ├── index.peb                  # Homepage
│       └── error.peb                  # Página de error
│
│
├── build.gradle.kts                   # Configuración Gradle
├── settings.gradle.kts
└── README.md                          # Este archivo
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
- **MySQL / MariaDB** (Producción)
- **JPA/Hibernate** como ORM
- **DDL auto-generado** desde entidades

### Frontend
- **Pebble Templates** (Motor de plantillas)
- **Bootstrap 5** (Framework CSS)
- **Bootstrap Icons**

### Build Tool
- **Gradle 9.2.1** (con Kotlin DSL)

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

## 📝 Próximos Pasos (Roadmap)

### 🚧 En Desarrollo
- [ ] **CartController** - Endpoints para gestión del carrito
  - POST /carrito/add - Añadir producto
  - GET /carrito - Ver carrito
  - PUT /carrito/item/{id} - Actualizar cantidad
  - DELETE /carrito/item/{id} - Eliminar línea
  - POST /carrito/checkout - Finalizar compra
  
- [ ] **AuthController** - Sistema de autenticación
  - GET /login - Formulario de login
  - POST /login - Procesar login
  - GET /registro - Formulario de registro
  - POST /registro - Procesar registro
  - GET /logout - Cerrar sesión

- [ ] **Vistas del Carrito** (Pebble)
  - carrito/view.peb - Vista del carrito
  - carrito/checkout.peb - Proceso de compra

- [ ] **Vistas de Autenticación** (Pebble)
  - auth/login.peb - Formulario login
  - auth/registro.peb - Formulario registro

### 🎯 Mejoras Futuras
- [ ] Sistema de favoritos
- [ ] Notificaciones por email (confirmación de pedidos)
- [ ] Paginación de productos
- [ ] Filtros avanzados (precio, disponibilidad)
- [ ] Sistema de reseñas de productos
- [ ] Panel de estadísticas avanzado para admin
- [ ] Integración con pasarela de pago (Stripe)
- [ ] Historial de precios de productos

---

## 🧪 Testing

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar tests con reporte
./gradlew test jacocoTestReport
```

---

## 📄 Licencia

Este proyecto tiene fines educativos y está desarrollado como parte del módulo de Desarrollo de Aplicaciones Web (2DAW).

---

## 👨‍💻 Autor

**Proyecto MediaDaw**  
Desarrollado por: Ángel Sánchez Gasanz  
Módulo: Desarrollo Web en Entorno Servidor (2DAW)  
Año: 2025-2026

---

<div align="center">

**⭐ Si te ha gustado el proyecto, dale una estrella ⭐**

Desarrollado con ❤️ y mucho ☕

</div>
