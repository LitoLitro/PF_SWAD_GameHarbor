package com.gameharbor.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** DTOs de carrito, ordenes, biblioteca, resenas, historial y recomendaciones. */
public final class TiendaDtos {

    private TiendaDtos() {}

    // ---- Carrito ----
    public record AgregarCarritoRequest(@NotNull Integer juegoId) {}

    public record CarritoItemDto(Integer juegoId, String titulo, String imagenUrl, BigDecimal precio, int cantidad) {}

    public record CarritoDto(List<CarritoItemDto> items, BigDecimal total) {}

    // ---- Compra ----
    public record CompraRequest(@NotNull Integer metodoPagoId) {}

    public record OrdenDetalleDto(Integer juegoId, String titulo, BigDecimal precioUnitario, int cantidad) {}

    public record OrdenDto(Integer id, LocalDateTime fecha, BigDecimal total, String estado,
                           String metodoPago, List<OrdenDetalleDto> detalles) {}

    // ---- Biblioteca ----
    public record BibliotecaItemDto(Integer juegoId, String titulo, String imagenUrl,
                                    boolean requiereCloud, LocalDateTime fechaAdquisicion) {}

    // ---- Resenas ----
    public record ResenaRequest(
            @Min(1) @Max(5) int calificacion,
            @Size(max = 500) String comentario) {}

    // ---- Historial / recomendaciones ----
    public record HistorialRequest(
            @NotNull Integer juegoId,
            @Min(0) int segundos,
            @NotNull @Pattern(regexp = "CATALOGO|BUSQUEDA|BIBLIOTECA|RECOMENDACION",
                    message = "origen debe ser CATALOGO, BUSQUEDA, BIBLIOTECA o RECOMENDACION") String origen) {}

    public record RecomendacionDto(CatalogoDtos.JuegoResumenDto juego, BigDecimal puntaje, String origen) {}
}
