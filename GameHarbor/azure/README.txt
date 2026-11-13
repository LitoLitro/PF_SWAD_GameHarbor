# ☁️ Azure — GameHarbor

Carpeta que contiene la **configuración de despliegue** del proyecto en **Microsoft Azure**.

## ¿Qué hace esta carpeta?
Almacena los archivos necesarios para publicar el sistema en la nube: configuración del App Service, pipeline CI/CD y variables de entorno de producción.

## Estructura principal
azure/
├── app-service-config.json → configuración del backend en Azure App Service
├── pipeline.yml → pipeline de CI/CD (GitHub Actions o Azure DevOps)
└── README.md

## Servicios de Azure utilizados
| Servicio | Uso en el proyecto |
|---|---|
| **Azure App Service** | Hospeda el backend Java (Spring Boot) |
| **Azure SQL Database** | Base de datos SQL Server en la nube |
| **Azure Static Web Apps** | Hospeda el frontend (HTML/CSS/JS) |
| **Azure Container Apps** | Hospeda el microservicio de Machine Learning |
| **Azure Monitor** | Métricas y logs de las sesiones de juego en la nube |

## Flujo de despliegue
1. El desarrollador hace `push` a la rama `develop`.
2. El pipeline (`pipeline.yml`) se ejecuta:
   - Compila el backend (`mvn package`).
   - Ejecuta las pruebas (`mvn test`).
   - Empaqueta el frontend.
   - Despliega en Azure App Service y Static Web Apps.
3. La base de datos se migra con los scripts de `database/`.

## Variables de entorno de producción
Se configuran en Azure Portal (nunca en el repo):
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `ML_API_URL`
- `JWT_SECRET`

## Responsable
Integrante 5.