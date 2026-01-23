# 🎭 Tests E2E con Playwright - MediaDaw

## 📋 Configuración Actual

Los tests están configurados para:
- ✅ **Solo Chromium** (no Firefox ni Safari)
- ✅ **Modo Headed** (navegador visible)
- ✅ **Velocidad reducida** (500ms entre acciones)
- ✅ **Screenshots** en caso de fallo
- ✅ **Videos** cuando los tests fallan

## 🚀 Ejecutar los Tests

### Opción 1: Ejecutar todos los tests (modo visual)
```bash
cd playwright-test
npx playwright test
```

### Opción 2: Ejecutar un test específico
```bash
npx playwright test mediadaw.spec.ts
```

### Opción 3: Modo UI interactivo (recomendado)
```bash
npx playwright test --ui
```
Este modo te permite:
- Ver los tests en una interfaz gráfica
- Ejecutar tests individuales
- Ver paso a paso cada acción
- Depurar tests fácilmente

### Opción 4: Modo debug (paso a paso)
```bash
npx playwright test --debug
```
Pausa la ejecución para que puedas inspeccionar cada paso.

## ⚙️ Configuraciones Adicionales

### Cambiar la velocidad de ejecución
Edita `playwright.config.ts` y modifica:
```typescript
slowMo: 500, // Milisegundos entre acciones
```
- `0` = Velocidad normal (rápido)
- `500` = Media velocidad (recomendado para ver)
- `1000` = Lento (1 segundo entre acciones)
- `2000` = Muy lento (para demostraciones)

### Usar Google Chrome en lugar de Chromium
En `playwright.config.ts`, descomenta:
```typescript
channel: 'chrome',
```

### Volver a modo headless (sin interfaz)
En `playwright.config.ts`, cambia:
```typescript
headless: true,
```

## 📊 Ver Reportes

Después de ejecutar los tests:
```bash
npx playwright show-report
```

## 🐛 Depuración

### Ver qué está seleccionando el test
```bash
npx playwright codegen http://localhost:8080
```
Esto abre un navegador y genera código mientras interactúas con la página.

### Inspeccionar selectores
```bash
npx playwright inspector
```

## 📝 Tests Disponibles

1. **Home y Productos** - Verifica que la página carga y muestra productos
2. **Login** - Prueba el inicio de sesión con credenciales válidas
3. **Añadir al Carrito** - Flujo completo de añadir producto al carrito

## ⚠️ Requisitos Previos

1. **Aplicación corriendo**: Asegúrate de que MediaDaw esté ejecutándose en `http://localhost:8080`
2. **Datos de prueba**: Los tests usan usuarios demo:
   - Admin: `admin@mediadaw.com` / `admin123`
   - Cliente: `cliente@mediadaw.com` / `cliente123`

## 🔧 Instalar Playwright (si no está instalado)

```bash
cd playwright-test
npm init playwright@latest
```

O si ya tienes el proyecto:
```bash
npm install
npx playwright install
```
