# 🎮 GameHarbor

Plataforma web de **compra y venta de videojuegos** estilo Steam, con la particularidad de permitir **jugar en la nube** sin necesidad de descargar ni instalar los juegos.

## 🎯 Problemática que resuelve
Unifica en un solo ecosistema la **compra digital de videojuegos** y el **cloud gaming**, eliminando la necesidad de hardware potente, largas descargas e instalaciones, y ofreciendo recomendaciones personalizadas mediante **Machine Learning**.

## 🏗️ Arquitectura
- **Backend:** Java + Spring Boot (MVC)
- **Frontend:** HTML + CSS + JavaScript
- **Base de datos:** SQL Server (desarrollo) / Azure SQL Database (producción)
- **Machine Learning:** Python + Scikit-learn + Flask/FastAPI
- **Despliegue:** Microsoft Azure

## 📂 Estructura del repositorio
GameHarbor/
├── backend/ → código Java del servidor
├── frontend/ → interfaz HTML/CSS/JS
├── machine-learning/ → modelo ML y su API
├── database/ → scripts SQL (schema, views, SPs)
├── tests/ → pruebas automatizadas
├── docs/ → documentación y manuales
└── azure/ → configuración de despliegue

## 🚀 Puesta en marcha rápida
```bash
# 1) Base de datos
# Ejecutar en SSMS: schema.sql → views.sql → stored-procedures.sql

# 2) Backend
cd backend && mvn spring-boot:run

# 3) Frontend
# Abrir frontend/index.html con Live Server

# 4) Machine Learning (opcional)
cd machine-learning && pip install -r requirements.txt && python app.py