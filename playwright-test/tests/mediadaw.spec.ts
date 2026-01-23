import { test, expect } from '@playwright/test';

/**
 * Tests E2E con Playwright para la aplicación MediaDaw
 */

test.describe('MediaDaw - Tests E2E', () => {

  /**
   * Test 1: Visitante - Ver la Home y Productos
   */
  test('Debe cargar la home y mostrar productos', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveTitle(/MediaDaw/);
    await expect(page.locator('nav')).toBeVisible();
    const productos = page.locator('.card');
    await expect(productos.first()).toBeVisible();
  });

  /**
   * Test 2: Usuario - Login Correcto
   */
  test('Debe permitir hacer login a un usuario registrado', async ({ page }) => {
    await page.goto('/login');

    await page.waitForLoadState('networkidle');
    await page.waitForSelector('form', { state: 'visible' });

    const usernameInput = page.locator('input[name="username"]');
    const passwordInput = page.locator('input[name="password"]');

    await usernameInput.fill('admin@mediadaw.com');
    await passwordInput.fill('admin123');

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle', timeout: 15000 }),
      passwordInput.press('Enter')
    ]);

    await expect(page).toHaveURL(/\/(productos)?(\?.*)?$/, { timeout: 10000 });

    await expect(page.locator('a[href="/login"]').filter({ hasText: 'Iniciar Sesión' })).not.toBeVisible({ timeout: 5000 });

    const userDropdown = page.locator('header .dropdown .dropdown-toggle');
    await expect(userDropdown).toBeVisible({ timeout: 5000 });
    await expect(userDropdown).toContainText('Admin');
  });

  /**
   * Test 3: Añadir al Carrito
   */
  test('Usuario logueado puede añadir producto al carrito', async ({ page }) => {
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
    await page.waitForSelector('form', { state: 'visible' });

    const usernameInput = page.locator('input[name="username"]');
    const passwordInput = page.locator('input[name="password"]');

    await usernameInput.fill('cliente@mediadaw.com');
    await passwordInput.fill('cliente123');

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle', timeout: 15000 }),
      passwordInput.press('Enter')
    ]);

    await expect(page).toHaveURL(/\/(productos)?(\?.*)?$/, { timeout: 10000 });
    await expect(page.locator('a[href="/login"]').filter({ hasText: 'Iniciar Sesión' })).not.toBeVisible({ timeout: 5000 });

    await page.goto('/productos');
    await page.waitForLoadState('networkidle');

    await page.waitForSelector('.product-card', { timeout: 10000 });

    const addButton = page.locator('button[type="submit"]').filter({ hasText: /añadir/i }).first();

    await addButton.scrollIntoViewIfNeeded();
    await expect(addButton).toBeVisible({ timeout: 5000 });

    await Promise.all([
      page.waitForResponse(response => response.url().includes('/carrito/add') && response.status() === 302, { timeout: 10000 }),
      addButton.click()
    ]);


    //Espero a que la pagina se actualice
    await page.waitForLoadState('networkidle');

    const cartBadge = page.locator('a[href="/carrito"] .badge');
    await expect(cartBadge).toBeVisible({ timeout: 5000 });
    await expect(cartBadge).toContainText(/\d+/);
  });

});
