

## 📁 `database/README.md`

```markdown
# 🗄️ Base de Datos — GameHarbor

Carpeta que contiene **todos los scripts SQL** del proyecto, correspondientes a la base de datos **GameHarbor** en **SQL Server**.

## ¿Qué hace esta carpeta?
Define la estructura de la base de datos, los datos de prueba, las vistas y los procedimientos almacenados que el backend utiliza para operar el sistema.

## Estructura principal
database/
├── GameHarborDB.sql → creación de tablas (3FN)
├── GameHarboriNSERTS.sql → datos de ejemplo
├── GameHarborViews.sql → vistas (VW_)
├── GameHarborProcedimientosAlmacenados.sql → procedimientos almacenados (SP_)


## Cómo ejecutar (SSMS)
1. Abrir SQL Server Management Studio.
2. Ejecutar en orden:


## Modelo de datos
- **18 tablas** en Tercera Forma Normal (3FN).
- **Claves primarias** enteras con `IDENTITY`.
- **Nombres de tabla** en MAYÚSCULAS.
- **10 vistas** para reportes y consultas del frontend.
- **14 procedimientos almacenados** para lógica transaccional (compras, sesiones cloud, login, recomendaciones).

## Módulos cubiertos
| Módulo | Tablas principales |
|---|---|
| Usuarios | `USUARIO`, `ROL`, `USUARIO_ROL` |
| Catálogo | `JUEGO`, `CATEGORIA`, `DESARROLLADOR`, `RESENA` |
| Compras | `CARRITO`, `ORDEN`, `ORDEN_DETALLE`, `BIBLIOTECA` |
| Cloud | `SERVIDOR_CLOUD`, `SESION_JUEGO` |
| ML | `HISTORIAL_VISTA`, `RECOMENDACION` |

## Responsables
- **Todos** los integrantes mantienen las tablas de su módulo.
- **Integrante 5** consolida los scripts y los despliega en **Azure SQL Database**.

## Notas
- No se debe modificar `schema.sql` sin avisar al equipo.
- Los SPs y vistas nuevas se agregan a `stored-procedures.sql` y `views.sql` respectivamente.
- Los hashes de contraseña en `data.sql` son de ejemplo; en producción se genera con BCrypt desde Java.