package com.gameharbor.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class CloudDtos {

    private CloudDtos() {}

    @Schema(description = "Juego adquirido que se desea iniciar en cloud gaming.")
    public record IniciarSesionRequest(@NotNull @Positive Integer juegoId) {}

    /** Metricas opcionales: si no se envian, el backend simula valores plausibles. */
    @Schema(description = "Métricas opcionales de cierre; si se omiten, el backend las simula.")
    public record FinalizarSesionRequest(
            @DecimalMin("0.0") BigDecimal bitrateMbps,
            @Min(0) Integer latenciaMs) {}

    @Schema(description = "Estado, servidor y métricas de una sesión Cloud simulada.")
    public record SesionDto(
            Integer id,
            Integer juegoId,
            String juegoTitulo,
            String servidor,
            String ubicacion,
            String estado,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            BigDecimal bitrateMbps,
            Integer latenciaMs) {}
}
