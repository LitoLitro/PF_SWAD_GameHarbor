package com.gameharbor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameharbor.model.entity.*;
import com.gameharbor.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.Matchers.*;
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
}
