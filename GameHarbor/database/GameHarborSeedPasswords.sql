/* =====================================================================
   PROYECTO : GameHarbor
   ARCHIVO  : GameHarborSeedPasswords.sql
   DESCRIPCION: Reemplaza los hashes de prueba de GameHarborInserts.sql por
                hashes BCrypt reales, para poder iniciar sesion desde la API Java.
   EJECUTAR : despues de GameHarborInserts.sql.
   CLAVE    : Test1234!   (para admin, lucia, diego y ana) - SOLO DESARROLLO
   ===================================================================== */

USE GameHarbor;
GO

UPDATE USUARIO
SET USUARIO_PASSWORD_HASH = '$2b$10$0pxb9f4N.KouAg8w9hwYMekFUgsvjoje1iK151u34VDniSTmEOD.a'
WHERE USUARIO_EMAIL IN ('admin@gameharbor.com', 'lucia@gameharbor.com',
                        'diego@gameharbor.com', 'ana@gameharbor.com');
GO
