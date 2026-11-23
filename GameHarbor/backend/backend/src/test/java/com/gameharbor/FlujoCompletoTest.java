package com.gameharbor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameharbor.model.entity.*;
import com.gameharbor.repository.*;
import com.gameharbor.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Recorre el caso de uso central: registrarse -> ver catalogo -> carrito -> comprar -> biblioteca -> jugar en la nube.
 * Usa H2 (perfil "test"), no necesita SQL Server.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlujoCompletoTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired RolRepository roles;
    @Autowired DesarrolladorRepository desarrolladores;
    @Autowired CategoriaRepository categorias;
    @Autowired JuegoRepository juegos;
    @Autowired MetodoPagoRepository metodosPago;
    @Autowired ServidorCloudRepository servidores;
    @Autowired UsuarioRepository usuarios;
    @Autowired BibliotecaRepository bibliotecas;
    @Autowired SesionJuegoRepository sesiones;
    @Autowired JwtUtil jwtUtil;

    @Value("${app.jwt.secret}") String jwtSecret;

    Integer tetrisId;
    Integer metodoPagoId;

    @BeforeEach
    void datos() {
        if (roles.findByNombre("CLIENTE").isEmpty()) {
            Rol r = new Rol();
            r.setNombre("CLIENTE");
            roles.save(r);

            Desarrollador d = new Desarrollador();
            d.setNombre("Nimbus Studios");
            desarrolladores.save(d);

            Categoria arcade = new Categoria();
            arcade.setNombre("Arcade");
            categorias.save(arcade);

            Juego tetris = new Juego();
            tetris.setTitulo("Tetris");
            tetris.setPrecio(new BigDecimal("9.90"));
            tetris.setDesarrollador(d);
            tetris.setCategorias(Set.of(arcade));
            juegos.save(tetris);

            MetodoPago yape = new MetodoPago();
            yape.setNombre("Yape");
            metodosPago.save(yape);

            ServidorCloud s = new ServidorCloud();
            s.setNombre("CLOUD-LIM-01");
            s.setUbicacion("Lima, Perú");
            s.setIp("10.0.0.1");
            s.setCapacidad(5);
            servidores.save(s);
        }
        tetrisId = juegos.findAll().get(0).getId();
        metodoPagoId = metodosPago.findAll().get(0).getId();
    }

    @AfterEach
    void restaurarServidorCloudBase() {
        List<ServidorCloud> existentes = servidores.findAll();
        for (ServidorCloud servidor : existentes) {
            boolean base = "CLOUD-LIM-01".equals(servidor.getNombre());
            servidor.setEstado(base ? "ACTIVO" : "INACTIVO");
            if (base) servidor.setCapacidad(5);
        }
        servidores.saveAll(existentes);
    }

    @Test
    void comprarTetrisYJugarloEnLaNube() throws Exception {
        // 1) Registro
        String registro = """
                {"nombre":"Ratio","email":"ratio@test.com","password":"Clave12345"}""";
        MvcResult res = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.roles[0]").value("CLIENTE"))
                .andReturn();
        String token = "Bearer " + json.readTree(res.getResponse().getContentAsString()).get("token").asText();

        // correo duplicado -> 409
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isConflict());

        // login bueno / malo
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ratio@test.com\",\"password\":\"Clave12345\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ratio@test.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());

        // 2) Catalogo publico (sin token) con busqueda
        mvc.perform(get("/api/juegos").param("q", "tet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].titulo").value("Tetris"))
                .andExpect(jsonPath("$.contenido[0].categorias[0]").value("Arcade"));
        mvc.perform(get("/api/juegos/" + tetrisId)).andExpect(status().isOk());

        // rutas privadas sin token -> 401
        mvc.perform(get("/api/carrito")).andExpect(status().isUnauthorized());

        // 3) Carrito
        mvc.perform(post("/api/carrito/items").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"juegoId\":" + tetrisId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.total").value(9.90));

        // no puede jugar algo que aun no compro
        mvc.perform(post("/api/cloud/sesiones").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"juegoId\":" + tetrisId + "}"))
                .andExpect(status().isForbidden());

        // 4) Compra
        mvc.perform(post("/api/ordenes").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"metodoPagoId\":" + metodoPagoId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PAGADA"))
                .andExpect(jsonPath("$.detalles[0].titulo").value("Tetris"));

        mvc.perform(get("/api/carrito").header("Authorization", token))
                .andExpect(jsonPath("$.items", hasSize(0)));

        // ya lo tiene: no se puede volver a agregar
        mvc.perform(post("/api/carrito/items").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"juegoId\":" + tetrisId + "}"))
                .andExpect(status().isConflict());

        // 5) Biblioteca
        mvc.perform(get("/api/biblioteca").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titulo").value("Tetris"));

        // 6) Jugar en la nube (simulado)
        MvcResult sesion = mvc.perform(post("/api/cloud/sesiones").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"juegoId\":" + tetrisId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.servidor").value("CLOUD-LIM-01"))
                .andReturn();
        JsonNode s = json.readTree(sesion.getResponse().getContentAsString());
        int sesionId = s.get("id").asInt();

        mvc.perform(get("/api/cloud/sesiones/activas").header("Authorization", token))
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(post("/api/cloud/sesiones/" + sesionId + "/finalizar").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADA"))
                .andExpect(jsonPath("$.latenciaMs").isNumber());

        // 7) Resena (compra verificada) y detalle con promedio
        mvc.perform(post("/api/juegos/" + tetrisId + "/resenas").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"calificacion\":5,\"comentario\":\"Clasico\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/juegos/" + tetrisId))
                .andExpect(jsonPath("$.calificacionPromedio").value(5.0))
                .andExpect(jsonPath("$.totalResenas").value(1));

        // validacion de entrada
        mvc.perform(post("/api/juegos/" + tetrisId + "/resenas").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"calificacion\":9}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void jwtDeUsuarioActivoSigueAutorizado() throws Exception {
        String token = registrarYObtenerToken("activo@test.com");

        mvc.perform(get("/api/carrito").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void jwtDeUsuarioDesactivadoEsRechazado() throws Exception {
        String token = registrarYObtenerToken("desactivado@test.com");
        Usuario usuario = usuarios.findByEmail("desactivado@test.com").orElseThrow();
        usuario.setEstado("BLOQUEADO");
        usuarios.save(usuario);

        mvc.perform(get("/api/carrito").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtInvalidoYExpiradoSonRechazados() throws Exception {
        mvc.perform(get("/api/carrito").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());

        Instant ahora = Instant.now();
        var key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String usuarioInexistente = Jwts.builder()
                .subject("inexistente@test.com")
                .claim("uid", 999999)
                .claim("roles", java.util.List.of("CLIENTE"))
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(60)))
                .signWith(key)
                .compact();

        mvc.perform(get("/api/carrito").header("Authorization", "Bearer " + usuarioInexistente))
                .andExpect(status().isUnauthorized());

        String expirado = Jwts.builder()
                .subject("expirado@test.com")
                .claim("uid", 999999)
                .claim("roles", java.util.List.of("CLIENTE"))
                .issuedAt(Date.from(ahora.minusSeconds(120)))
                .expiration(Date.from(ahora.minusSeconds(60)))
                .signWith(key)
                .compact();

        mvc.perform(get("/api/carrito").header("Authorization", "Bearer " + expirado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkoutConcurrenteSoloGeneraUnaCompraYConservaLaIntegridad() throws Exception {
        String token = registrarYObtenerToken("compra-concurrente@test.com");
        mvc.perform(post("/api/carrito/items").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"juegoId\":" + tetrisId + "}"))
                .andExpect(status().isOk());

        CountDownLatch listos = new CountDownLatch(2);
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Integer> checkout = () -> {
            listos.countDown();
            inicio.await();
            return mvc.perform(post("/api/ordenes").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"metodoPagoId\":" + metodoPagoId + "}"))
                    .andReturn().getResponse().getStatus();
        };

        try {
            Future<Integer> primera = executor.submit(checkout);
            Future<Integer> segunda = executor.submit(checkout);
            listos.await(5, TimeUnit.SECONDS);
            inicio.countDown();

            int estado1 = primera.get(20, TimeUnit.SECONDS);
            int estado2 = segunda.get(20, TimeUnit.SECONDS);
            assertTrue((estado1 == 201 && estado2 == 400) || (estado1 == 400 && estado2 == 201),
                    "Una compra debe completarse y la segunda encontrar el carrito vacío");
        } finally {
            inicio.countDown();
            executor.shutdownNow();
        }

        mvc.perform(get("/api/ordenes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/biblioteca").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/carrito").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void iniciosConcurrentesRespetanLaCapacidadDelServidor() throws Exception {
        ServidorCloud servidor = prepararServidorUnico(1, "capacidad");
        String token1 = registrarYAdquirirJuego("cloud-capacidad-1@test.com");
        String token2 = registrarYAdquirirJuego("cloud-capacidad-2@test.com");

        List<MvcResult> resultados = iniciarCloudSimultaneamente(token1, token2);
        assertEquals(1, resultados.stream().filter(r -> r.getResponse().getStatus() == 201).count());
        assertEquals(1, resultados.stream().filter(r -> r.getResponse().getStatus() == 503).count());
        long activas = sesiones.countByServidorIdAndEstado(servidor.getId(), "ACTIVA");
        assertTrue(activas <= servidor.getCapacidad(), "El servidor no debe exceder su capacidad");
    }

    @Test
    void iniciosConcurrentesDelMismoUsuarioYJuegoDevuelvenUnaSolaSesion() throws Exception {
        ServidorCloud servidor = prepararServidorUnico(1, "duplicado");
        String token = registrarYAdquirirJuego("cloud-duplicado@test.com");

        List<MvcResult> resultados = iniciarCloudSimultaneamente(token, token);
        assertTrue(resultados.stream().allMatch(r -> r.getResponse().getStatus() == 201));
        int id1 = json.readTree(resultados.get(0).getResponse().getContentAsString()).get("id").asInt();
        int id2 = json.readTree(resultados.get(1).getResponse().getContentAsString()).get("id").asInt();
        assertEquals(id1, id2);
        assertEquals(1, sesiones.countByServidorIdAndEstado(servidor.getId(), "ACTIVA"));
    }

    @Test
    void finalizarSesionLiberaCupoParaOtroUsuario() throws Exception {
        ServidorCloud servidor = prepararServidorUnico(1, "reutilizacion");
        String token1 = registrarYAdquirirJuego("cloud-finaliza-1@test.com");
        String token2 = registrarYAdquirirJuego("cloud-finaliza-2@test.com");

        MvcResult primera = mvc.perform(post("/api/cloud/sesiones").header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"juegoId\":" + tetrisId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        int primeraId = json.readTree(primera.getResponse().getContentAsString()).get("id").asInt();

        mvc.perform(post("/api/cloud/sesiones/" + primeraId + "/finalizar")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADA"));

        mvc.perform(post("/api/cloud/sesiones").header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"juegoId\":" + tetrisId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.servidor").value(servidor.getNombre()));
        assertEquals(1, sesiones.countByServidorIdAndEstado(servidor.getId(), "ACTIVA"));
    }

    @Test
    void validacionesRechazanIdentificadoresYParametrosFueraDeRango() throws Exception {
        String token = registrarYObtenerToken("validaciones@test.com");

        mvc.perform(post("/api/carrito/items").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"juegoId\":0}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/carrito/items").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"juegoId\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/ordenes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"metodoPagoId\":0}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/ordenes").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"metodoPagoId\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/juegos/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/juegos/-1")).andExpect(status().isBadRequest());

        mvc.perform(get("/api/juegos").param("q", "x".repeat(151)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/juegos").param("categoriaId", "0"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/juegos").param("categoriaId", "-1"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/juegos").param("pagina", "-1"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/juegos").param("tamano", "51"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/juegos").param("tamano", "50"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/recomendaciones").header("Authorization", "Bearer " + token)
                        .param("limite", "31"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"validaciones@test.com\",\"password\":\"" + "x".repeat(73) + "\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/juegos/" + tetrisId + "/resenas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calificacion\":5,\"comentario\":\"" + "x".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validacionesDeHistorialYMetricasMantienenRangosDefinidos() throws Exception {
        String token = registrarYObtenerToken("rangos@test.com");
        mvc.perform(post("/api/historial").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"juegoId\":" + tetrisId + ",\"segundos\":86401,\"origen\":\"CATALOGO\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/historial").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"juegoId\":" + tetrisId + ",\"segundos\":86400,\"origen\":\"CATALOGO\"}"))
                .andExpect(status().isCreated());

        String cloudToken = registrarYAdquirirJuego("metricas@test.com");
        MvcResult inicioCloud = mvc.perform(post("/api/cloud/sesiones").header("Authorization", "Bearer " + cloudToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"juegoId\":" + tetrisId + "}"))
                .andExpect(status().isCreated()).andReturn();
        int sesionId = json.readTree(inicioCloud.getResponse().getContentAsString()).get("id").asInt();
        mvc.perform(post("/api/cloud/sesiones/" + sesionId + "/finalizar")
                        .header("Authorization", "Bearer " + cloudToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bitrateMbps\":-0.1,\"latenciaMs\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void errores400401Y403UsanLaMismaEstructuraJson() throws Exception {
        String token = registrarYObtenerToken("errores@test.com");

        mvc.perform(post("/api/carrito/items").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"juegoId\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").isNotEmpty())
                .andExpect(jsonPath("$.detalle").isNotEmpty());

        mvc.perform(get("/api/carrito"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").isNotEmpty());

        Usuario usuario = usuarios.findByEmail("errores@test.com").orElseThrow();
        String tokenRolNoPermitido = "Bearer " + jwtUtil.generar(usuario.getId(), usuario.getEmail(), List.of("OTRO"));
        mvc.perform(get("/api/carrito").header("Authorization", tokenRolNoPermitido))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    private String registrarYObtenerToken(String email) throws Exception {
        String registro = "{\"nombre\":\"Usuario\",\"email\":\"" + email
                + "\",\"password\":\"Clave12345\"}";
        MvcResult resultado = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode respuesta = json.readTree(resultado.getResponse().getContentAsString());
        return respuesta.get("token").asText();
    }

    private ServidorCloud prepararServidorUnico(int capacidad, String sufijo) {
        List<ServidorCloud> existentes = servidores.findAll();
        for (ServidorCloud servidor : existentes) {
            servidor.setEstado("INACTIVO");
        }
        servidores.saveAll(existentes);

        String unique = java.util.UUID.randomUUID().toString();
        ServidorCloud servidor = new ServidorCloud();
        servidor.setNombre("TEST-CLOUD-" + sufijo + "-" + unique.substring(0, 8));
        servidor.setUbicacion("Test");
        servidor.setIp("test-" + unique);
        servidor.setCapacidad(capacidad);
        servidor.setEstado("ACTIVO");
        return servidores.save(servidor);
    }

    private String registrarYAdquirirJuego(String email) throws Exception {
        String token = registrarYObtenerToken(email);
        Usuario usuario = usuarios.findByEmail(email).orElseThrow();
        Biblioteca item = new Biblioteca();
        item.setUsuario(usuario);
        item.setJuego(juegos.getReferenceById(tetrisId));
        bibliotecas.save(item);
        return token;
    }

    private List<MvcResult> iniciarCloudSimultaneamente(String token1, String token2) throws Exception {
        CountDownLatch listos = new CountDownLatch(2);
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<MvcResult> request1 = solicitudInicioCloud(token1, listos, inicio);
        Callable<MvcResult> request2 = solicitudInicioCloud(token2, listos, inicio);
        try {
            Future<MvcResult> futura1 = executor.submit(request1);
            Future<MvcResult> futura2 = executor.submit(request2);
            assertTrue(listos.await(5, TimeUnit.SECONDS), "Las solicitudes deben quedar listas");
            inicio.countDown();
            return List.of(futura1.get(20, TimeUnit.SECONDS), futura2.get(20, TimeUnit.SECONDS));
        } finally {
            inicio.countDown();
            executor.shutdownNow();
        }
    }

    private Callable<MvcResult> solicitudInicioCloud(String token, CountDownLatch listos, CountDownLatch inicio) {
        return () -> {
            listos.countDown();
            if (!inicio.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Inicio concurrente agotado");
            return mvc.perform(post("/api/cloud/sesiones").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"juegoId\":" + tetrisId + "}"))
                    .andReturn();
        };
    }
}
