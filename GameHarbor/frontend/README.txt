

## 📁 `frontend/README.md`

```markdown
# 🎨 Frontend — GameHarbor

Carpeta que contiene toda la **interfaz de usuario** del proyecto: HTML, CSS y JavaScript que el usuario ve y con la que interactúa desde el navegador.

## ¿Qué hace esta carpeta?
Presenta las pantallas del sistema (login, catálogo, carrito, biblioteca, juego en la nube, recomendaciones) y consume los endpoints del backend mediante `fetch`/AJAX.

## Estructura principal

frontend/
├── index.html → página principal
├── css/ → hojas de estilo por módulo
├── js/ → lógica JavaScript por módulo
├── img/ → imágenes, íconos y carátulas
└── pages/ → páginas HTML organizadas por módulo
├── auth/ → login, registro, perfil
├── catalogo/ → listado, detalle, búsqueda
├── carrito/ → carrito y checkout
├── orden/ → historial y detalle de orden
├── biblioteca/ → mi biblioteca
├── cloud/ → lanzar sesión y reproductor
└── recomendaciones/ → recomendados para ti


## Tecnologías
- HTML5
- CSS3
- JavaScript (vanilla, sin frameworks)
- Fetch API para consumo del backend

## Cómo visualizar (desarrollo)
1. Levantar el backend (`mvn spring-boot:run`).
2. Abrir `frontend/index.html` en el navegador o servirlo con una extensión tipo **Live Server** de VS Code.

## Convenciones
- Los archivos CSS y JS se nombran igual que su módulo: `catalogo.css`, `catalogo.js`, etc.
- Cada página debe consumir **solo endpoints del backend**, nunca conectarse directo a SQL Server.
- Todas las rutas relativas usan `/css`, `/js`, `/img`.

## Responsables por módulo
| Módulo | Integrante |
|---|---|
| Login, registro, perfil | Integrante 1 |
| Catálogo y búsqueda | Integrante 2 |
| Carrito, checkout, biblioteca | Integrante 3 |
| Juego en la nube y reproductor | Integrante 4 |
| Recomendaciones | Integrante 5 |