package com.gameharbor.controller;

import com.gameharbor.config.AuthUser;
import com.gameharbor.model.dto.TiendaDtos.AgregarCarritoRequest;
import com.gameharbor.model.dto.TiendaDtos.CarritoDto;
import com.gameharbor.service.CarritoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Carrito", description = "Consulta y modificación del carrito del usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carrito;

    public CarritoController(CarritoService carrito) {
        this.carrito = carrito;
    }

    @GetMapping
    @Operation(summary = "Ver carrito", description = "Devuelve los artículos y el total del carrito propio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito devuelto", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT")
    })
    public CarritoDto ver(@AuthenticationPrincipal AuthUser user) {
        return carrito.obtener(user.id());
    }

    @PostMapping("/items")
    @Operation(summary = "Agregar juego", description = "Agrega un juego disponible que aún no pertenezca al usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artículo agregado o carrito actualizado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Identificador inválido"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT"),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado"),
            @ApiResponse(responseCode = "409", description = "Juego no disponible o ya adquirido")
    })
    public CarritoDto agregar(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody AgregarCarritoRequest req) {
        return carrito.agregar(user.id(), req.juegoId());
    }

    @DeleteMapping("/items/{juegoId}")
    @Operation(summary = "Quitar juego", description = "Quita el juego indicado del carrito propio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito actualizado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT")
    })
    public CarritoDto quitar(@AuthenticationPrincipal AuthUser user, @PathVariable Integer juegoId) {
        return carrito.quitar(user.id(), juegoId);
    }

    @DeleteMapping
    @Operation(summary = "Vaciar carrito", description = "Elimina todos los artículos del carrito propio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito vaciado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT")
    })
    public CarritoDto vaciar(@AuthenticationPrincipal AuthUser user) {
        return carrito.vaciar(user.id());
    }
}
