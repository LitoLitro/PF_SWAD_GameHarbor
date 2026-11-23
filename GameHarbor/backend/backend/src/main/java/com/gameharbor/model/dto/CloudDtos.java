package com.gameharbor.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class CloudDtos {

    private CloudDtos() {}

    public record IniciarSesionRequest(@NotNull @Positive Integer juegoId) {}

    /** Metricas opcionales: si no se envian, el backend simula valores plausibles. */
    public record FinalizarSesionRequest(
            @DecimalMin("0.0") BigDecimal bitrateMbps,
            @Min(0) Integer latenciaMs) {}

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
