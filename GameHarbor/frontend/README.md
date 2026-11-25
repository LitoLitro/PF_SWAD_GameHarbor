# GameHarbor frontend

Aplicación web de GameHarbor desarrollada con Angular standalone y TypeScript estricto. Las pantallas Angular viven en `src/`; el HTML/CSS/JavaScript original se conserva en `index.html`, `css/`, `js/` y `pages/` como referencia histórica, no como la aplicación Angular activa.

## Requisitos y ejecución

- Node.js 24.x
- npm 11.x
- Backend Spring Boot disponible en `http://localhost:8080`

```powershell
npm.cmd install
npm.cmd start
npm.cmd run build
```

La aplicación local se sirve en `http://localhost:4200`. La URL de la API está en `src/environments/environment.ts`; producción usa `src/environments/environment.production.ts`.

## Integración

`src/app/core/api/api.models.ts` declara los DTO TypeScript y `api.services.ts` agrupa los servicios de juegos/categorías, reseñas, carrito, métodos de pago, órdenes, biblioteca, Cloud, historial y recomendaciones. Las pantallas consultan los endpoints reales definidos por el backend. No se usa el archivo heredado `src/app/shared/demo-data.ts` para presentar juegos.

La compra y las sesiones Cloud son operaciones simuladas por el backend. Cloud no transmite video y las recomendaciones presentan solo los resultados ya almacenados por el backend.

## Autenticación y seguridad

El acceso usa `POST /api/auth/register` y `POST /api/auth/login`. La sesión JWT se conserva en `sessionStorage` durante la pestaña actual y se elimina al cerrar sesión o al detectar expiración. El interceptor adjunta el token solo a rutas API privadas conocidas. Los guards mejoran la navegación, mientras que la autorización real de datos y operaciones corresponde al backend.

El token accesible a JavaScript implica riesgo ante XSS; el frontend no debe incluir scripts o recursos no confiables. No se guardan contraseñas ni secretos en el código fuente.

## Pruebas

`npm.cmd run build` valida la compilación. Aunque `package.json` conserva un script `test`, este proyecto no tiene un target `test` configurado en `angular.json` ni archivos de especificación Angular; `npm.cmd test` por tanto no ejecuta una suite automatizada. Las verificaciones HTTP y de flujos autenticados deben hacerse contra el backend usando una cuenta QA controlada.

## Pruebas automatizadas y de navegador

```powershell
npm.cmd test
npm.cmd run test:e2e
```

`npm.cmd test` usa el builder `@angular/build:unit-test` con Vitest y jsdom. Las pruebas unitarias simulan HTTP con `HttpTestingController` y no requieren SQL Server.

`npm.cmd run test:e2e` usa Playwright/Chromium y verifica la portada, catálogo real, búsqueda, filtro, detalle, menú móvil, validación de rutas privadas y pantallas privadas con respuestas HTTP simuladas. Necesita el backend en `http://localhost:8080`. La primera ejecución requiere instalar Chromium con `npx.cmd playwright install chromium`. Las capturas y artefactos se guardan en la carpeta temporal del sistema, fuera del repositorio. Los flujos de compra y publicación no se envían desde estas pruebas.
