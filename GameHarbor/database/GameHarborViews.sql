/* =====================================================================
   PROYECTO : GameHarbor
   ARCHIVO  : views.sql
   DESCRIPCIÓN: Vistas (VIEW) para reportes y consultas reutilizables.
   ===================================================================== */

USE GameHarbor;
GO

/* ---------------------------------------------------------------------
   VW_CATALOGO_JUEGOS
   Catálogo completo con desarrollador y categorías concatenadas.
   Útil para la página principal y la búsqueda.
   --------------------------------------------------------------------- */
CREATE OR ALTER VIEW VW_CATALOGO_JUEGOS AS
SELECT
    J.JUEGO_ID,
    J.JUEGO_TITULO,
    J.JUEGO_DESCRIPCION,
    J.JUEGO_PRECIO,
    J.JUEGO_FECHA_LANZAMIENTO,
    J.JUEGO_IMAGEN_URL,
    J.JUEGO_REQUIERE_CLOUD,
    J.JUEGO_ESTADO,
    D.DESARROLLADOR_ID,
    D.DESARROLLADOR_NOMBRE,
    D.DESARROLLADOR_PAIS,
    STRING_AGG(C.CATEGORIA_NOMBRE, ', ') AS CATEGORIAS
FROM JUEGO J
INNER JOIN DESARROLLADOR D ON J.DESARROLLADOR_ID = D.DESARROLLADOR_ID
LEFT JOIN JUEGO_CATEGORIA JC ON J.JUEGO_ID = JC.JUEGO_ID
LEFT JOIN CATEGORIA C ON JC.CATEGORIA_ID = C.CATEGORIA_ID
GROUP BY
    J.JUEGO_ID, J.JUEGO_TITULO, J.JUEGO_DESCRIPCION, J.JUEGO_PRECIO,
    J.JUEGO_FECHA_LANZAMIENTO, J.JUEGO_IMAGEN_URL, J.JUEGO_REQUIERE_CLOUD,
    J.JUEGO_ESTADO, D.DESARROLLADOR_ID, D.DESARROLLADOR_NOMBRE, D.DESARROLLADOR_PAIS;
GO

/* ---------------------------------------------------------------------
   VW_DETALLE_JUEGO
   Detalle del juego con promedio de calificación y número de reseñas.
   --------------------------------------------------------------------- */
CREATE OR ALTER VIEW VW_DETALLE_JUEGO AS
SELECT
    J.JUEGO_ID,
    J.JUEGO_TITULO,
    J.JUEGO_DESCRIPCION,
    J.JUEGO_PRECIO,
    J.JUEGO_ESTADO,
    J.JUEGO_REQUIERE_CLOUD,
    D.DESARROLLADOR_NOMBRE,
    ISNULL(AVG(CAST(R.RESENA_CALIFICACION AS DECIMAL(3,2))), 0) AS PROMEDIO_CALIFICACION,
    COUNT(R.RESENA_ID) AS TOTAL_RESENAS
FROM JUEGO J
INNER JOIN DESARROLLADOR D ON J.DESARROLLADOR_ID = D.DESARROLLADOR_ID
LEFT JOIN RESENA R ON J.JUEGO_ID = R.JUEGO_ID
GROUP BY
    J.JUEGO_ID, J.JUEGO_TITULO, J.JUEGO_DESCRIPCION, J.JUEGO_PRECIO,
    J.JUEGO_ESTADO, J.JUEGO_REQUIERE_CLOUD, D.DESARROLLADOR_NOMBRE;
GO

/* ---------------------------------------------------------------------
   VW_BIBLIOTECA_USUARIO
   Juegos que posee cada usuario, listos para jugar en la nube.
   --------------------------------------------------------------------- */
CREATE OR ALTER VIEW VW_BIBLIOTECA_USUARIO AS
SELECT
    B.BIBLIOTECA_ID,
    U.USUARIO_ID,
    U.USUARIO_NOMBRE,
    U.USUARIO_EMAIL,
    J.JUEGO_ID,
    J.JUEGO_TITULO,
    J.JUEGO_IMAGEN_URL,
    J.JUEGO_REQUIERE_CLOUD,
    B.BIBLIOTECA_FECHA_ADQUISICION
FROM BIBLIOTECA B
INNER JOIN USUARIO U ON B.USUARIO_ID = U.USUARIO_ID
INNER JOIN JUEGO   J ON B.JUEGO_ID   = J.JUEGO_ID;
GO

/* ---------------------------------------------------------------------
   VW_CARRITO_USUARIO
   Contenido del carrito de cada usuario con subtotales.
   --------------------------------------------------------------------- */
CREATE OR ALTER VIEW VW_CARRITO_USUARIO AS
SELECT
    C.CARRITO_ID,
    U.USUARIO_ID,
    U.USUARIO_NOMBRE,
    CI.CARRITO_ITEM_ID,
    J.JUEGO_ID,
    J.JUEGO_TITULO,
    J.JUEGO_PRECIO,
    CI.CARRITO_ITEM_CANTIDAD,
    (J.JUEGO_PRECIO * CI.CARRITO_ITEM_CANTIDAD) AS SUBTOTAL
FROM CARRITO C
INNER JOIN USUARIO      U  ON C.USUARIO_ID   = U.USUARIO_ID
INNER JOIN CARRITO_ITEM CI ON C.CARRITO_ID   = CI.CARRITO_ID
INNER JOIN JUEGO        J  ON CI.JUEGO_ID    = J.JUEGO_ID;
GO

/* ---------------------------------------------------------------------
   VW_HISTORIAL_ORDENES
   Historial de órdenes por usuario con total y estado.
   --------------------------------------------------------------------- */
CREATE OR ALTER VIEW VW_HISTORIAL_ORDENES AS
SELECT
    O.ORDEN_ID,
    U.USUARIO_ID,
    U.USUARIO_NOMBRE,
    U.USUARIO_EMAIL,
    O.ORDEN_FECHA,
    O.ORDEN_TOTAL,
    O.ORDEN_ESTADO,
    MP.METODO_PAGO_NOMBRE,
    (SELECT COUNT(*) FROM ORDEN_DETALLE OD WHERE OD.ORDEN_ID = O.ORDEN_ID) AS CANTIDAD_JUEGOS
FROM ORDEN O
INNER JOIN USUARIO       U  ON O.USUARIO_ID     = U.USUARIO_ID
INNER JOIN METODO_PAGO   MP ON O.METODO_PAGO_ID = MP.METODO_PAGO_ID;
GO

/* ---------------------------------------------------------------------
   VW_DETALLE_ORDEN
   Detalle línea por línea de cada orden.
   --------------------------------------------------------------------- */
CREATE OR ALTER VIEW VW_DETALLE_ORDEN AS
SELECT
    OD.ORDEN_DETALLE_ID,
    OD.ORDEN_ID,
    J.JUEGO_ID,
    J.JUEGO_TITULO,
    OD.ORDEN_DETALLE_PRECIO_UNITARIO,
    OD.ORDEN_DETALLE_CANTIDAD,
    (OD.ORDEN_DETALLE_PRECIO_UNITARIO * OD.ORDEN_DETALLE_CANTIDAD) AS SUBTOTAL
FROM ORDEN_DETALLE OD
INNER JOIN JUEGO J ON OD.JUEGO_ID = J.JUEGO_ID;
GO

/* ---------------------------------------------------------------------
   VW_RECOMENDACIONES_USUARIO
   Recomendaciones activas por usuario, listas para mostrar en el front.
   --------------------------------------------------------------------- */
CREATE OR ALTER VIEW VW_RECOMENDACIONES_USUARIO AS
SELECT
    R.RECOMENDACION_ID,
    U.USUARIO_ID,
    U.USUARIO_NOMBRE,
    J.JUEGO_ID,
    J.JUEGO_TITULO,
    J.JUEGO_PRECIO,
    J.JUEGO_IMAGEN_URL,
    R.RECOMENDACION_PUNTAJE,
    R.RECOMENDACION_ORIGEN,
    R.RECOMENDACION_FECHA
FROM RECOMENDACION R
INNER JOIN USUARIO U ON R.USUARIO_ID = U.USUARIO_ID
INNER JOIN JUEGO   J ON R.JUEGO_ID   = J.JUEGO_ID;
GO

/* ---------------------------------------------------------------------
   VW_SESIONES_ACTIVAS
   Sesiones de juego en la nube activas, con datos del servidor.
   --------------------------------------------------------------------- */
CREATE OR ALTER VIEW VW_SESIONES_ACTIVAS AS
SELECT
    S.SESION_ID,
    U.USUARIO_ID,
    U.USUARIO_NOMBRE,
    J.JUEGO_ID,
    J.JUEGO_TITULO,
    SC.SERVIDOR_CLOUD_ID,
    SC.SERVIDOR_CLOUD_NOMBRE,
    SC.SERVIDOR_CLOUD_UBICACION,
    S.SESION_FECHA_INICIO,
    S.SESION_BITRATE_MBPS,
    S.SESION_LATENCIA_MS,
    DATEDIFF(MINUTE, S.SESION_FECHA_INICIO, SYSDATETIME()) AS MINUTOS_TRANSCURRIDOS
FROM SESION_JUEGO S
INNER JOIN USUARIO        U  ON S.USUARIO_ID        = U.USUARIO_ID
INNER JOIN JUEGO          J  ON S.JUEGO_ID          = J.JUEGO_ID
INNER JOIN SERVIDOR_CLOUD SC ON S.SERVIDOR_CLOUD_ID = SC.SERVIDOR_CLOUD_ID
WHERE S.SESION_ESTADO = 'ACTIVA';
GO

/* ---------------------------------------------------------------------
   VW_TOP_JUEGOS
   Ranking de juegos por promedio de calificación y número de ventas.
   --------------------------------------------------------------------- */
CREATE OR ALTER VIEW VW_TOP_JUEGOS AS
SELECT
    J.JUEGO_ID,
    J.JUEGO_TITULO,
    J.JUEGO_PRECIO,
    ISNULL(AVG(CAST(R.RESENA_CALIFICACION AS DECIMAL(3,2))), 0) AS PROMEDIO_CALIFICACION,
    COUNT(DISTINCT R.RESENA_ID)         AS TOTAL_RESENAS,
    COUNT(DISTINCT OD.ORDEN_DETALLE_ID) AS VECES_VENDIDO
FROM JUEGO J
LEFT JOIN RESENA        R  ON J.JUEGO_ID = R.JUEGO_ID
LEFT JOIN ORDEN_DETALLE OD ON J.JUEGO_ID = OD.JUEGO_ID
GROUP BY J.JUEGO_ID, J.JUEGO_TITULO, J.JUEGO_PRECIO;
GO

/* ---------------------------------------------------------------------
   VW_ESTADISTICAS_JUEGO
   Datos agregados por juego: ingresos, sesiones cloud y promedio de latencia.
   --------------------------------------------------------------------- */
CREATE OR ALTER VIEW VW_ESTADISTICAS_JUEGO AS
SELECT
    J.JUEGO_ID,
    J.JUEGO_TITULO,
    ISNULL(SUM(OD.ORDEN_DETALLE_PRECIO_UNITARIO * OD.ORDEN_DETALLE_CANTIDAD), 0) AS INGRESOS_TOTALES,
    COUNT(DISTINCT S.SESION_ID) AS TOTAL_SESIONES,
    ISNULL(AVG(S.SESION_LATENCIA_MS), 0) AS LATENCIA_PROMEDIO_MS
FROM JUEGO J
LEFT JOIN ORDEN_DETALLE OD ON J.JUEGO_ID = OD.JUEGO_ID
LEFT JOIN SESION_JUEGO  S  ON J.JUEGO_ID = S.JUEGO_ID
GROUP BY J.JUEGO_ID, J.JUEGO_TITULO;
GO

PRINT 'Vistas creadas correctamente en GameHarbor.';
GO
