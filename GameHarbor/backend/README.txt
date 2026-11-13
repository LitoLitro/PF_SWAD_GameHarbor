# 🖥️ Backend — GameHarbor

Carpeta que contiene todo el **código del servidor** del proyecto GameHarbor, desarrollado en **Java** con **Spring Boot (Spring MVC)**.

## ¿Qué hace esta carpeta?
Implementa la **capa lógica y de negocio** del sistema: recibe las peticiones del frontend, valida datos, ejecuta las reglas de negocio y se comunica con la base de datos SQL Server y con el servicio de Machine Learning.

## Estructura principal

backend/
├── pom.xml → dependencias Maven
└── src/main/java/com/gameharbor/
├── config/ → configuración global (seguridad, conexión, Azure)
├── controller/ → controladores MVC y endpoints REST
├── service/ → lógica de negocio
├── repository/ → acceso a datos (Spring Data JPA)
├── model/ → entidades y DTOs
├── exception/ → manejo centralizado de errores
└── util/ → utilidades comunes


## Tecnologías
- Java 17+
- Spring Boot 3 + Spring MVC
- Spring Data JPA
- SQL Server / Azure SQL
- Maven

## Cómo ejecutar (desarrollo)
```bash
cd backend
mvn spring-boot:run