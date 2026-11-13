# 🧪 Pruebas — GameHarbor

Carpeta que contiene **todas las pruebas automatizadas** del proyecto: unitarias, de integración y de extremo a extremo (E2E).

## ¿Qué hace esta carpeta?
Valida que el backend, el frontend, el modelo ML y los scripts de base de datos funcionen correctamente antes de desplegar a producción.

## Estructura principal

tests/
├── backend/ → pruebas JUnit + Mockito
│ ├── UsuarioServiceTest.java
│ ├── JuegoServiceTest.java
│ ├── CarritoServiceTest.java
│ ├── SesionCloudServiceTest.java
│ └── RecomendacionServiceTest.java
├── frontend/
│ └── e2e/ → pruebas con Selenium o Playwright
└── machine-learning/
└── test_model.py → pruebas del modelo


## Tipos de pruebas
| Tipo | Herramienta | Qué valida |
|---|---|---|
| Unitarias | JUnit 5 + Mockito | Servicios y lógica de negocio |
| Integración | Spring Boot Test | Controller ↔ Service ↔ Repository |
| E2E | Selenium / Playwright | Flujo completo en el navegador |
| ML | Pytest | Precisión del modelo y API |

## Cómo ejecutar
```bash
# Backend
cd backend
mvn test

# Machine Learning
cd machine-learning
pytest