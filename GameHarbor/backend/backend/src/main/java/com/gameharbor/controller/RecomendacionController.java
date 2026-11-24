package com.gameharbor.controller;

import com.gameharbor.config.AuthUser;
import com.gameharbor.model.dto.TiendaDtos.HistorialRequest;
import com.gameharbor.model.dto.TiendaDtos.RecomendacionDto;
import com.gameharbor.service.RecomendacionService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@Tag(name = "Recomendaciones", description = "Lectura de recomendaciones existentes y registro de vistas.")
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api")
public class RecomendacionController {

    private final RecomendacionService recomendaciones;

    public RecomendacionController(RecomendacionService recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    @GetMapping("/recomendaciones")
    @Operation(summary = "Obtener recomendaciones", description = "Devuelve las recomendaciones almacenadas para el usuario; la generación externa de ML no forma parte de este endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recomendaciones devueltas", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Límite fuera del rango 1–30"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT")
    })
    public List<RecomendacionDto> recomendaciones(@AuthenticationPrincipal AuthUser user,
                                                  @Min(1) @Max(30) @RequestParam(defaultValue = "10") int limite) {
        return recomendaciones.para(user.id(), limite);
    }

    /** El frontend avisa cuando el usuario ve un juego; alimenta el modelo de ML. */
    @PostMapping("/historial")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar vista", description = "Registra una interacción con un juego para alimentar el historial del usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vista registrada"),
            @ApiResponse(responseCode = "400", description = "Datos de historial inválidos"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT"),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado")
    })
    public void registrarVista(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody HistorialRequest req) {
        recomendaciones.registrarVista(user.id(), req);
    }
}
