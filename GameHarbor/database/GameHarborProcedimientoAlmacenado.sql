/* =====================================================================
   PROYECTO : GameHarbor
   ARCHIVO  : stored-procedures.sql
   DESCRIPCIÓN: Procedimientos almacenados para la lógica de negocio.
   ===================================================================== */

USE GameHarbor;
GO

/* =====================================================================
   MÓDULO 1 — USUARIOS
   ===================================================================== */

/* ---------------------------------------------------------------------
   SP_LOGIN_USUARIO
   Valida credenciales. Devuelve los datos del usuario y sus roles.
   NOTA: la comparación del hash se hace aquí por simplicidad académica;
   en producción el hash se valida en la capa Java (BCrypt).
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_LOGIN_USUARIO
    @EMAIL    NVARCHAR(150),
    @PASSWORD NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (
        SELECT 1 FROM USUARIO
        WHERE USUARIO_EMAIL = @EMAIL
          AND USUARIO_PASSWORD_HASH = @PASSWORD
          AND USUARIO_ESTADO = 'ACTIVO'
    )
    BEGIN
        SELECT CAST(0 AS BIT) AS EXITO, 'Credenciales inválidas o usuario inactivo' AS MENSAJE;
        RETURN;
    END

    SELECT
        CAST(1 AS BIT) AS EXITO,
        'Login exitoso' AS MENSAJE,
        U.USUARIO_ID,
        U.USUARIO_NOMBRE,
        U.USUARIO_EMAIL,
        U.USUARIO_SALDO,
        STRING_AGG(R.ROL_NOMBRE, ', ') AS ROLES
    FROM USUARIO U
    INNER JOIN USUARIO_ROL UR ON U.USUARIO_ID = UR.USUARIO_ID
    INNER JOIN ROL         R  ON UR.ROL_ID    = R.ROL_ID
    WHERE U.USUARIO_EMAIL = @EMAIL
    GROUP BY U.USUARIO_ID, U.USUARIO_NOMBRE, U.USUARIO_EMAIL, U.USUARIO_SALDO;
END
GO

/* ---------------------------------------------------------------------
   SP_REGISTRAR_USUARIO
   Crea un usuario, su carrito y le asigna el rol CLIENTE por defecto.
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_REGISTRAR_USUARIO
    @NOMBRE   NVARCHAR(80),
    @EMAIL    NVARCHAR(150),
    @PASSWORD NVARCHAR(255),
    @USUARIO_ID INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF EXISTS (SELECT 1 FROM USUARIO WHERE USUARIO_EMAIL = @EMAIL)
    BEGIN
        RAISERROR('El correo ya está registrado.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO USUARIO (USUARIO_NOMBRE, USUARIO_EMAIL, USUARIO_PASSWORD_HASH)
        VALUES (@NOMBRE, @EMAIL, @PASSWORD);

        SET @USUARIO_ID = SCOPE_IDENTITY();

        -- Asignar rol CLIENTE
        INSERT INTO USUARIO_ROL (USUARIO_ID, ROL_ID)
        SELECT @USUARIO_ID, ROL_ID FROM ROL WHERE ROL_NOMBRE = 'CLIENTE';

        -- Crear carrito activo
        INSERT INTO CARRITO (USUARIO_ID) VALUES (@USUARIO_ID);

        COMMIT TRANSACTION;
        SELECT CAST(1 AS BIT) AS EXITO, @USUARIO_ID AS USUARIO_ID, 'Usuario registrado' AS MENSAJE;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
        SELECT CAST(0 AS BIT) AS EXITO, ERROR_MESSAGE() AS MENSAJE;
    END CATCH
END
GO

/* =====================================================================
   MÓDULO 3 — CARRITO Y COMPRAS
   ===================================================================== */

/* ---------------------------------------------------------------------
   SP_AGREGAR_CARRITO
   Agrega un juego al carrito del usuario (o incrementa cantidad).
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_AGREGAR_CARRITO
    @USUARIO_ID INT,
    @JUEGO_ID   INT,
    @CANTIDAD   INT = 1
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @CARRITO_ID INT;

    SELECT @CARRITO_ID = CARRITO_ID FROM CARRITO WHERE USUARIO_ID = @USUARIO_ID;

    IF @CARRITO_ID IS NULL
    BEGIN
        INSERT INTO CARRITO (USUARIO_ID) VALUES (@USUARIO_ID);
        SET @CARRITO_ID = SCOPE_IDENTITY();
    END

    -- Validar que el juego exista y esté disponible
    IF NOT EXISTS (SELECT 1 FROM JUEGO WHERE JUEGO_ID = @JUEGO_ID AND JUEGO_ESTADO = 'DISPONIBLE')
    BEGIN
        RAISERROR('Juego no disponible.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM CARRITO_ITEM WHERE CARRITO_ID = @CARRITO_ID AND JUEGO_ID = @JUEGO_ID)
        UPDATE CARRITO_ITEM
        SET CARRITO_ITEM_CANTIDAD = CARRITO_ITEM_CANTIDAD + @CANTIDAD
        WHERE CARRITO_ID = @CARRITO_ID AND JUEGO_ID = @JUEGO_ID;
    ELSE
        INSERT INTO CARRITO_ITEM (CARRITO_ID, JUEGO_ID, CARRITO_ITEM_CANTIDAD)
        VALUES (@CARRITO_ID, @JUEGO_ID, @CANTIDAD);

    SELECT CAST(1 AS BIT) AS EXITO, 'Juego agregado al carrito' AS MENSAJE;
END
GO

/* ---------------------------------------------------------------------
   SP_ELIMINAR_CARRITO
   Elimina un juego específico del carrito.
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_ELIMINAR_CARRITO
    @USUARIO_ID INT,
    @JUEGO_ID   INT
AS
BEGIN
    SET NOCOUNT ON;

    DELETE CI
    FROM CARRITO_ITEM CI
    INNER JOIN CARRITO C ON CI.CARRITO_ID = C.CARRITO_ID
    WHERE C.USUARIO_ID = @USUARIO_ID AND CI.JUEGO_ID = @JUEGO_ID;

    SELECT CAST(1 AS BIT) AS EXITO, 'Juego eliminado del carrito' AS MENSAJE;
END
GO

/* ---------------------------------------------------------------------
   SP_VACIAR_CARRITO
   Vacía por completo el carrito de un usuario.
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_VACIAR_CARRITO
    @USUARIO_ID INT
AS
BEGIN
    SET NOCOUNT ON;

    DELETE CI
    FROM CARRITO_ITEM CI
    INNER JOIN CARRITO C ON CI.CARRITO_ID = C.CARRITO_ID
    WHERE C.USUARIO_ID = @USUARIO_ID;

    SELECT CAST(1 AS BIT) AS EXITO, 'Carrito vaciado' AS MENSAJE;
END
GO

/* ---------------------------------------------------------------------
   SP_PROCESAR_COMPRA
   Convierte el carrito en una ORDEN, inserta en ORDEN_DETALLE y BIBLIOTECA,
   y vacía el carrito. Todo en una transacción.
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_PROCESAR_COMPRA
    @USUARIO_ID     INT,
    @METODO_PAGO_ID INT,
    @ORDEN_ID       INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @CARRITO_ID INT, @TOTAL DECIMAL(10,2);

    SELECT @CARRITO_ID = CARRITO_ID FROM CARRITO WHERE USUARIO_ID = @USUARIO_ID;

    IF @CARRITO_ID IS NULL OR NOT EXISTS (SELECT 1 FROM CARRITO_ITEM WHERE CARRITO_ID = @CARRITO_ID)
    BEGIN
        RAISERROR('El carrito está vacío.', 16, 1);
        RETURN;
    END

    SELECT @TOTAL = SUM(J.JUEGO_PRECIO * CI.CARRITO_ITEM_CANTIDAD)
    FROM CARRITO_ITEM CI
    INNER JOIN JUEGO J ON CI.JUEGO_ID = J.JUEGO_ID
    WHERE CI.CARRITO_ID = @CARRITO_ID;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 1) Crear la cabecera de la orden
        INSERT INTO ORDEN (USUARIO_ID, METODO_PAGO_ID, ORDEN_TOTAL, ORDEN_ESTADO)
        VALUES (@USUARIO_ID, @METODO_PAGO_ID, @TOTAL, 'PAGADA');

        SET @ORDEN_ID = SCOPE_IDENTITY();

        -- 2) Crear el detalle de la orden
        INSERT INTO ORDEN_DETALLE (ORDEN_ID, JUEGO_ID, ORDEN_DETALLE_PRECIO_UNITARIO, ORDEN_DETALLE_CANTIDAD)
        SELECT @ORDEN_ID, CI.JUEGO_ID, J.JUEGO_PRECIO, CI.CARRITO_ITEM_CANTIDAD
        FROM CARRITO_ITEM CI
        INNER JOIN JUEGO J ON CI.JUEGO_ID = J.JUEGO_ID
        WHERE CI.CARRITO_ID = @CARRITO_ID;

        -- 3) Agregar a la biblioteca (evitando duplicados)
        INSERT INTO BIBLIOTECA (USUARIO_ID, JUEGO_ID)
        SELECT DISTINCT @USUARIO_ID, CI.JUEGO_ID
        FROM CARRITO_ITEM CI
        WHERE CI.CARRITO_ID = @CARRITO_ID
          AND NOT EXISTS (
              SELECT 1 FROM BIBLIOTECA B
              WHERE B.USUARIO_ID = @USUARIO_ID AND B.JUEGO_ID = CI.JUEGO_ID
          );

        -- 4) Vaciar carrito
        DELETE FROM CARRITO_ITEM WHERE CARRITO_ID = @CARRITO_ID;

        COMMIT TRANSACTION;

        SELECT CAST(1 AS BIT) AS EXITO, @ORDEN_ID AS ORDEN_ID, @TOTAL AS TOTAL, 'Compra realizada con éxito' AS MENSAJE;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
        SELECT CAST(0 AS BIT) AS EXITO, ERROR_MESSAGE() AS MENSAJE;
    END CATCH
END
GO

/* =====================================================================
   MÓDULO 4 — JUEGO EN LA NUBE
   ===================================================================== */

/* ---------------------------------------------------------------------
   SP_INICIAR_SESION_CLOUD
   Valida que el usuario posea el juego y crea una sesión en un servidor
   ACTIVO con capacidad disponible.
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_INICIAR_SESION_CLOUD
    @USUARIO_ID INT,
    @JUEGO_ID   INT,
    @SESION_ID  INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    -- Verificar propiedad del juego
    IF NOT EXISTS (SELECT 1 FROM BIBLIOTECA WHERE USUARIO_ID = @USUARIO_ID AND JUEGO_ID = @JUEGO_ID)
    BEGIN
        RAISERROR('El usuario no posee este juego en su biblioteca.', 16, 1);
        RETURN;
    END

    -- Verificar que el juego requiera/esté habilitado para la nube
    IF NOT EXISTS (SELECT 1 FROM JUEGO WHERE JUEGO_ID = @JUEGO_ID AND JUEGO_ESTADO = 'DISPONIBLE')
    BEGIN
        RAISERROR('Juego no disponible para jugar.', 16, 1);
        RETURN;
    END

    -- Elegir servidor con menor carga (sesiones activas < capacidad)
    DECLARE @SERVIDOR_ID INT;

    SELECT TOP 1 @SERVIDOR_ID = SC.SERVIDOR_CLOUD_ID
    FROM SERVIDOR_CLOUD SC
    LEFT JOIN SESION_JUEGO S
        ON S.SERVIDOR_CLOUD_ID = SC.SERVIDOR_CLOUD_ID AND S.SESION_ESTADO = 'ACTIVA'
    WHERE SC.SERVIDOR_CLOUD_ESTADO = 'ACTIVO'
    GROUP BY SC.SERVIDOR_CLOUD_ID, SC.SERVIDOR_CLOUD_CAPACIDAD
    HAVING COUNT(S.SESION_ID) < SC.SERVIDOR_CLOUD_CAPACIDAD
    ORDER BY COUNT(S.SESION_ID) ASC;

    IF @SERVIDOR_ID IS NULL
    BEGIN
        RAISERROR('No hay servidores disponibles en este momento.', 16, 1);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO SESION_JUEGO (USUARIO_ID, JUEGO_ID, SERVIDOR_CLOUD_ID, SESION_ESTADO)
        VALUES (@USUARIO_ID, @JUEGO_ID, @SERVIDOR_ID, 'ACTIVA');

        SET @SESION_ID = SCOPE_IDENTITY();

        COMMIT TRANSACTION;

        SELECT CAST(1 AS BIT) AS EXITO, @SESION_ID AS SESION_ID, @SERVIDOR_ID AS SERVIDOR_CLOUD_ID,
               'Sesión iniciada' AS MENSAJE;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
        SELECT CAST(0 AS BIT) AS EXITO, ERROR_MESSAGE() AS MENSAJE;
    END CATCH
END
GO

/* ---------------------------------------------------------------------
   SP_FINALIZAR_SESION_CLOUD
   Cierra una sesión activa y guarda métricas de bitrate/latencia.
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_FINALIZAR_SESION_CLOUD
    @SESION_ID       INT,
    @BITRATE_MBPS    DECIMAL(6,2) = NULL,
    @LATENCIA_MS     INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM SESION_JUEGO WHERE SESION_ID = @SESION_ID AND SESION_ESTADO = 'ACTIVA')
    BEGIN
        RAISERROR('La sesión no existe o ya fue finalizada.', 16, 1);
        RETURN;
    END

    UPDATE SESION_JUEGO
    SET SESION_ESTADO       = 'FINALIZADA',
        SESION_FECHA_FIN    = SYSDATETIME(),
        SESION_BITRATE_MBPS = ISNULL(@BITRATE_MBPS, SESION_BITRATE_MBPS),
        SESION_LATENCIA_MS  = ISNULL(@LATENCIA_MS, SESION_LATENCIA_MS)
    WHERE SESION_ID = @SESION_ID;

    SELECT CAST(1 AS BIT) AS EXITO, 'Sesión finalizada' AS MENSAJE;
END
GO

/* =====================================================================
   MÓDULO 2 — CATÁLOGO / RESEÑAS
   ===================================================================== */

/* ---------------------------------------------------------------------
   SP_AGREGAR_RESENA
   Permite reseñar un juego que el usuario ya posee (compra verificada).
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_AGREGAR_RESENA
    @USUARIO_ID     INT,
    @JUEGO_ID       INT,
    @CALIFICACION   TINYINT,
    @COMENTARIO     NVARCHAR(500) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM BIBLIOTECA WHERE USUARIO_ID = @USUARIO_ID AND JUEGO_ID = @JUEGO_ID)
    BEGIN
        RAISERROR('Solo puedes reseñar juegos que ya posees.', 16, 1);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM RESENA WHERE USUARIO_ID = @USUARIO_ID AND JUEGO_ID = @JUEGO_ID)
    BEGIN
        UPDATE RESENA
        SET RESENA_CALIFICACION = @CALIFICACION,
            RESENA_COMENTARIO   = @COMENTARIO,
            RESENA_FECHA        = SYSDATETIME()
        WHERE USUARIO_ID = @USUARIO_ID AND JUEGO_ID = @JUEGO_ID;

        SELECT CAST(1 AS BIT) AS EXITO, 'Reseña actualizada' AS MENSAJE;
    END
    ELSE
    BEGIN
        INSERT INTO RESENA (USUARIO_ID, JUEGO_ID, RESENA_CALIFICACION, RESENA_COMENTARIO)
        VALUES (@USUARIO_ID, @JUEGO_ID, @CALIFICACION, @COMENTARIO);

        SELECT CAST(1 AS BIT) AS EXITO, 'Reseña registrada' AS MENSAJE;
    END
END
GO

/* =====================================================================
   MÓDULO 5 — MACHINE LEARNING
   ===================================================================== */

/* ---------------------------------------------------------------------
   SP_REGISTRAR_VISTA
   Registra la visita de un usuario a un juego (input del modelo ML).
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_REGISTRAR_VISTA
    @USUARIO_ID INT,
    @JUEGO_ID   INT,
    @SEGUNDOS   INT = 0,
    @ORIGEN     NVARCHAR(20) = 'CATALOGO'
AS
BEGIN
    SET NOCOUNT ON;

    IF @ORIGEN NOT IN ('CATALOGO','BUSQUEDA','RECOMENDACION','BIBLIOTECA')
        SET @ORIGEN = 'CATALOGO';

    INSERT INTO HISTORIAL_VISTA (USUARIO_ID, JUEGO_ID, HISTORIAL_SEGUNDOS, HISTORIAL_ORIGEN)
    VALUES (@USUARIO_ID, @JUEGO_ID, @SEGUNDOS, @ORIGEN);

    SELECT CAST(1 AS BIT) AS EXITO, 'Vista registrada' AS MENSAJE;
END
GO

/* ---------------------------------------------------------------------
   SP_OBTENER_RECOMENDACIONES
   Devuelve las recomendaciones activas del usuario ordenadas por puntaje.
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_OBTENER_RECOMENDACIONES
    @USUARIO_ID INT,
    @TOP        INT = 10
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP (@TOP)
        J.JUEGO_ID,
        J.JUEGO_TITULO,
        J.JUEGO_PRECIO,
        J.JUEGO_IMAGEN_URL,
        R.RECOMENDACION_PUNTAJE,
        R.RECOMENDACION_ORIGEN
    FROM RECOMENDACION R
    INNER JOIN JUEGO J ON R.JUEGO_ID = J.JUEGO_ID
    WHERE R.USUARIO_ID = @USUARIO_ID
      AND J.JUEGO_ESTADO = 'DISPONIBLE'
    ORDER BY R.RECOMENDACION_PUNTAJE DESC;
END
GO

/* ---------------------------------------------------------------------
   SP_REGENERAR_RECOMENDACIONES
   Ejemplo de recálculo simple (para ser reemplazado por el modelo ML).
   Recomienda los juegos mejor calificados que el usuario NO posee.
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_REGENERAR_RECOMENDACIONES
    @USUARIO_ID INT,
    @TOP        INT = 10
AS
BEGIN
    SET NOCOUNT ON;

    DELETE FROM RECOMENDACION WHERE USUARIO_ID = @USUARIO_ID AND RECOMENDACION_ORIGEN = 'POPULAR';

    ;WITH CANDIDATOS AS (
        SELECT TOP (@TOP)
            J.JUEGO_ID,
            ISNULL(AVG(CAST(R.RESENA_CALIFICACION AS DECIMAL(3,2))), 0) / 5.0 AS PUNTAJE
        FROM JUEGO J
        LEFT JOIN RESENA R ON J.JUEGO_ID = R.JUEGO_ID
        WHERE J.JUEGO_ESTADO = 'DISPONIBLE'
          AND NOT EXISTS (SELECT 1 FROM BIBLIOTECA B WHERE B.USUARIO_ID = @USUARIO_ID AND B.JUEGO_ID = J.JUEGO_ID)
        GROUP BY J.JUEGO_ID
        ORDER BY PUNTAJE DESC
    )
    INSERT INTO RECOMENDACION (USUARIO_ID, JUEGO_ID, RECOMENDACION_PUNTAJE, RECOMENDACION_ORIGEN)
    SELECT @USUARIO_ID, JUEGO_ID, CAST(PUNTAJE AS DECIMAL(5,4)), 'POPULAR'
    FROM CANDIDATOS;

    SELECT CAST(1 AS BIT) AS EXITO, 'Recomendaciones regeneradas' AS MENSAJE;
END
GO

/* =====================================================================
   CONSULTAS RÁPIDAS
   ===================================================================== */

/* ---------------------------------------------------------------------
   SP_OBTENER_BIBLIOTECA
   Devuelve la biblioteca del usuario con datos del juego.
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_OBTENER_BIBLIOTECA
    @USUARIO_ID INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT * FROM VW_BIBLIOTECA_USUARIO WHERE USUARIO_ID = @USUARIO_ID ORDER BY BIBLIOTECA_FECHA_ADQUISICION DESC;
END
GO

/* ---------------------------------------------------------------------
   SP_OBTENER_CARRITO
   Devuelve el contenido del carrito con subtotales.
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE SP_OBTENER_CARRITO
    @USUARIO_ID INT
AS
BEGIN
    SET NOCOUNT ON;
    SELECT * FROM VW_CARRITO_USUARIO WHERE USUARIO_ID = @USUARIO_ID;
END
GO

PRINT '✅ Procedimientos almacenados creados correctamente en GameHarbor.';
GO