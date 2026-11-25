package com.gameharbor.controller;

import com.gameharbor.config.AuthUser;
import com.gameharbor.model.dto.TiendaDtos.BibliotecaItemDto;
import com.gameharbor.model.dto.TiendaDtos.CompraRequest;
import com.gameharbor.model.dto.TiendaDtos.OrdenDto;
import com.gameharbor.service.OrdenService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Órdenes y biblioteca", description = "Compra simulada, historial de órdenes y biblioteca propia.")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api")
public class OrdenController {

    private final OrdenService ordenes;

    public OrdenController(OrdenService ordenes) {
        this.ordenes = ordenes;
    }

    /** Paga todo el carrito actual. */
    @PostMapping("/ordenes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Comprar carrito", description = "Crea una orden PAGADA de prueba, agrega los juegos a la biblioteca y vacía el carrito.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Método de pago inválido o carrito vacío"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT"),
            @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual de los juegos")
    })
    public OrdenDto comprar(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CompraRequest req) {
        return ordenes.comprar(user.id(), req.metodoPagoId());
    }

    @GetMapping("/ordenes")
    @Operation(summary = "Consultar historial de órdenes", description = "Devuelve únicamente las órdenes del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial devuelto", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT")
    })
    public List<OrdenDto> historial(@AuthenticationPrincipal AuthUser user) {
        return ordenes.historial(user.id());
    }

    @GetMapping("/biblioteca")
    @Operation(summary = "Consultar biblioteca", description = "Devuelve los juegos adquiridos por el usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Biblioteca devuelta", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT")
    })
    public List<BibliotecaItemDto> biblioteca(@AuthenticationPrincipal AuthUser user) {
        return ordenes.biblioteca(user.id());
    }
}
