# GameHarbor — Backend (Spring Boot)

API REST en **Java 17 + Spring Boot 3.5 + Spring Data JPA + SQL Server**, con autenticación **JWT**.
El esquema de la BD lo crean los scripts de `/database`; Hibernate **no** modifica tablas (`ddl-auto=none`).

## Cómo correrlo

1. En SSMS ejecutar en orden: `GameHarborDB.sql` → `GameHarborInserts.sql` → `GameHarborSeedPasswords.sql`
   (+ `GameHarborViews.sql` y `GameHarborProcedimientoAlmacenado.sql` si el equipo los usa).
2. Ajustar usuario/clave en `src/main/resources/application.properties`, o definir las variables
   de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`.
3. SQL Server debe tener TCP/IP habilitado en el puerto 1433.
4. En Spring Tool Suite: *File → Import → Maven → Existing Maven Projects* → carpeta `backend`,
   luego *Run As → Spring Boot App* (clase `GameHarborApplication`).
   Por consola: `mvn spring-boot:run`.

Pruebas (no necesitan SQL Server, usan H2 en memoria): `mvn test`.

Usuarios de prueba (después del script de claves): `lucia@gameharbor.com` / `Test1234!`
(también `diego@`, `ana@` y `admin@gameharbor.com`).

## Endpoints

Los marcados 🔒 requieren el header `Authorization: Bearer <token>`.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/register` | Crea usuario (rol CLIENTE + carrito) y devuelve token |
| POST | `/api/auth/login` | Devuelve token + datos del usuario |
| GET | `/api/juegos?q=&categoriaId=&pagina=0&tamano=12` | Catálogo con búsqueda y filtro |
| GET | `/api/juegos/{id}` | Detalle + calificación promedio |
| GET | `/api/juegos/{id}/resenas` | Reseñas del juego |
| POST 🔒 | `/api/juegos/{id}/resenas` | Reseñar (solo si lo posee; si ya reseñó, actualiza) |
| GET | `/api/categorias`, `/api/metodos-pago` | Listas de apoyo |
| GET 🔒 | `/api/carrito` | Ver carrito |
| POST 🔒 | `/api/carrito/items` `{juegoId}` | Agregar juego (digital: cantidad 1) |
| DELETE 🔒 | `/api/carrito/items/{juegoId}`, `/api/carrito` | Quitar uno / vaciar |
| POST 🔒 | `/api/ordenes` `{metodoPagoId}` | Pagar el carrito → orden + biblioteca |
| GET 🔒 | `/api/ordenes`, `/api/biblioteca` | Historial de compras / juegos del usuario |
| POST 🔒 | `/api/cloud/sesiones` `{juegoId}` | **Jugar** (sesión cloud simulada) |
| POST 🔒 | `/api/cloud/sesiones/{id}/finalizar` | Cierra sesión; simula bitrate/latencia si no se envían |
| GET 🔒 | `/api/cloud/sesiones/activas` | Sesiones activas del usuario |
| GET 🔒 | `/api/recomendaciones?limite=10` | Recomendaciones del modelo de ML (tabla `RECOMENDACION`) |
| POST 🔒 | `/api/historial` `{juegoId, segundos, origen}` | Registra vistas para alimentar el ML |

Errores: JSON `{timestamp, status, error, detalle?}`.

## Decisiones de diseño

- La lógica de negocio está en **Java** (`service/`), replicando los SP (`SP_REGISTRAR_USUARIO`,
  `SP_PROCESAR_COMPRA`, `SP_INICIAR_SESION_CLOUD`, …). Los SP quedan en la BD pero la API no los llama.
- Contraseñas con **BCrypt** en Java. `SP_LOGIN_USUARIO` compara texto plano, por eso no se usa.
- El pago es simulado: `SP_PROCESAR_COMPRA` tampoco descuenta `USUARIO_SALDO`, y aquí se mantiene igual.

## Pendiente

- Integración con el servicio de ML en Python (árbol de decisión) para llenar `RECOMENDACION`.
- Endpoints de administración (alta/edición de juegos) con rol `ADMINISTRADOR`.
- Despliegue en Azure (variables de entorno ya soportadas en `application.properties`).
