package com.gameharbor.controller;

import com.gameharbor.config.AuthUser;
import com.gameharbor.model.dto.CloudDtos.FinalizarSesionRequest;
import com.gameharbor.model.dto.CloudDtos.IniciarSesionRequest;
import com.gameharbor.model.dto.CloudDtos.SesionDto;
import com.gameharbor.service.CloudService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Cloud Gaming", description = "Gestión simulada de sesiones de juego en servidores cloud.")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/cloud/sesiones")
public class CloudController {

    private final CloudService cloud;

    public CloudController(CloudService cloud) {
        this.cloud = cloud;
    }

    /** "Jugar" un juego de la biblioteca: abre una sesion en un servidor cloud simulado. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Iniciar sesión Cloud", description = "Asigna un servidor con capacidad disponible a un juego de la biblioteca.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sesión iniciada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Identificador inválido"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT"),
            @ApiResponse(responseCode = "403", description = "El usuario no posee el juego"),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado"),
            @ApiResponse(responseCode = "409", description = "Juego no disponible"),
            @ApiResponse(responseCode = "503", description = "No hay capacidad disponible")
    })
    public SesionDto iniciar(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody IniciarSesionRequest req) {
        return cloud.iniciar(user.id(), req.juegoId());
    }

    @PostMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar sesión Cloud", description = "Finaliza una sesión propia; si no se envían métricas, se generan valores simulados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión finalizada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Métricas fuera de los rangos permitidos"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT"),
            @ApiResponse(responseCode = "404", description = "Sesión no encontrada"),
            @ApiResponse(responseCode = "409", description = "La sesión ya fue finalizada")
    })
    public SesionDto finalizar(@AuthenticationPrincipal AuthUser user,
                               @Parameter(description = "ID de la sesión propia") @PathVariable Integer id,
                               @Valid @RequestBody(required = false) FinalizarSesionRequest req) {
        return cloud.finalizar(user.id(), id, req);
    }

    @GetMapping("/activas")
    @Operation(summary = "Listar sesiones activas", description = "Devuelve las sesiones activas del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesiones devueltas", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT")
    })
    public List<SesionDto> activas(@AuthenticationPrincipal AuthUser user) {
        return cloud.activas(user.id());
    }
}
