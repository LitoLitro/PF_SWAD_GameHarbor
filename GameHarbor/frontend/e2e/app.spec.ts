import { expect, test } from '@playwright/test';
import { mkdir } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const screenshots = join(tmpdir(), 'gameharbor-responsive');
const fixturePassword = ['browser', 'mock', 'only'].join('-');
const privatePages = [
  { path: '/carrito', heading: 'Carrito', endpoint: '**/api/carrito', data: { items: [], total: 0 }, empty: 'Tu carrito' },
  { path: '/ordenes', heading: 'Historial de', endpoint: '**/api/ordenes', data: [], empty: 'Sin ' },
  { path: '/biblioteca', heading: 'Tu biblioteca', endpoint: '**/api/biblioteca', data: [], empty: 'Tu biblioteca' },
  { path: '/cloud', heading: 'Sesiones Cloud', endpoint: '**/api/cloud/sesiones/activas', data: [], empty: 'No tienes sesiones' },
  { path: '/recomendaciones', heading: 'Recomendaciones', endpoint: '**/api/recomendaciones?*', data: [], empty: 'Sin recomendaciones' },
];

function mockedSession(): string {
  const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 }));
  const token = `header.${payload}.signature`;
  return JSON.stringify({ token, expiresAt: (Math.floor(Date.now() / 1000) + 3600) * 1000, usuario: { id: 7001, nombre: 'QA visual', email: 'visual-qa@example.invalid', saldo: 0, roles: ['CLIENTE'] } });
}

test.beforeAll(async () => { await mkdir(screenshots, { recursive: true }); });

for (const width of [390, 768, 1440]) {
  test(`responsive public flows at ${width}px`, async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', error => errors.push(error.message));
    page.on('console', message => { if (message.type() === 'error') errors.push(message.text()); });
    await page.setViewportSize({ width, height: 900 });
    await page.goto('/');
    await expect(page.getByRole('heading', { level: 1 })).toContainText('Tu pr');
    await expect(page.getByText('Nimbus Runner', { exact: true })).toBeVisible();
    expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(width);
    await page.screenshot({ path: join(screenshots, `home-${width}.png`), fullPage: true });

    if (width < 900) {
      const menu = page.locator('.menu-toggle');
      await expect(menu).toBeVisible();
      await menu.click();
      await expect(menu).toHaveAttribute('aria-expanded', 'true');
      await page.locator('#primary-navigation a[href="/catalogo"]').click();
    } else {
      await page.locator('a[href="/catalogo"]').first().click();
    }
    await expect(page.locator('h1')).toContainText('Cat');
    await page.locator('#game-search').fill('nimbus');
    await page.getByRole('button', { name: 'Buscar' }).click();
    await expect(page.locator('a[href="/catalogo/1"]').first()).toBeVisible();
    await page.locator('#category-filter').selectOption({ index: 1 });
    await expect(page.locator('#category-filter')).not.toHaveValue('');
    await page.locator('a[href="/catalogo/1"]').first().click();
    await expect(page.locator('h1')).toContainText('Nimbus Runner');
    await expect(page.locator('h2').filter({ hasText: /^Res/ })).toBeVisible();
    expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(width);
    await page.screenshot({ path: join(screenshots, `detail-${width}.png`), fullPage: true });
    expect(errors, errors.join('\n')).toEqual([]);
  });
}

test('private routes redirect without a session and render mocked empty responses with a session', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 900 });
  await page.goto('/biblioteca');
  await expect(page).toHaveURL(/\/login\?returnUrl=/);
  await page.addInitScript(value => sessionStorage.setItem('gameharbor.auth.session', value), mockedSession());
  for (const view of privatePages) {
    await page.route(view.endpoint, route => route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(view.data) }));
  }
  for (const width of [390, 768, 1440]) {
    await page.setViewportSize({ width, height: 900 });
    for (const view of privatePages) {
      await page.goto(view.path);
      await expect(page.getByRole('heading', { level: 1 })).toContainText(view.heading);
      await expect(page.getByRole('heading', { level: 3 }).filter({ hasText: view.empty })).toBeVisible();
      expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(width);
      await page.screenshot({ path: join(screenshots, `${view.path.slice(1)}-${width}.png`), fullPage: true });
    }
  }
});

test('mocked login and logout clear the session without using real account credentials', async ({ page }) => {
  const token = `header.${btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 600 }))}.signature`;
  await page.route('**/api/auth/login', route => route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ token, usuario: { id: 7001, nombre: 'QA visual', email: 'visual-qa@example.invalid', saldo: 0, roles: ['CLIENTE'] } }) }));
  await page.goto('/login');
  await page.locator('#login-email').fill('visual-qa@example.invalid');
  await page.locator('#login-password').fill(fixturePassword);
  await page.locator('form button[type="submit"]').click();
  await expect(page.getByText('QA visual')).toBeVisible();
  await expect.poll(() => page.evaluate(() => sessionStorage.getItem('gameharbor.auth.session'))).toContain(token);
  await page.locator('.logout-link').click();
  await expect(page.locator('a.account-link[href="/login"]')).toBeVisible();
  expect(await page.evaluate(() => sessionStorage.getItem('gameharbor.auth.session'))).toBeNull();
  for (const width of [390, 768, 1440]) {
    await page.setViewportSize({ width, height: 900 });
    await page.goto('/login');
    await expect(page.locator('#login-email')).toBeVisible();
    await expect(page.locator('#login-password')).toBeVisible();
    await page.goto('/registro');
    await expect(page.locator('#register-name')).toBeVisible();
    expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(width);
  }
});
