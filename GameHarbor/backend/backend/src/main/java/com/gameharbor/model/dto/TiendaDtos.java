package com.gameharbor.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** DTOs de carrito, ordenes, biblioteca, resenas, historial y recomendaciones. */
public final class TiendaDtos {

    private TiendaDtos() {}

    // ---- Carrito ----
    @Schema(description = "Identificador del juego que se agregará al carrito.")
    public record AgregarCarritoRequest(@NotNull @Positive Integer juegoId) {}

    public record CarritoItemDto(Integer juegoId, String titulo, String imagenUrl, BigDecimal precio, int cantidad) {}

    @Schema(description = "Artículos y total del carrito del usuario.")
    public record CarritoDto(List<CarritoItemDto> items, BigDecimal total) {}

    // ---- Compra ----
    @Schema(description = "Método de pago elegido para la compra simulada.")
    public record CompraRequest(@NotNull @Positive Integer metodoPagoId) {}

    public record OrdenDetalleDto(Integer juegoId, String titulo, BigDecimal precioUnitario, int cantidad) {}

    @Schema(description = "Orden y detalle de los juegos adquiridos.")
    public record OrdenDto(Integer id, LocalDateTime fecha, BigDecimal total, String estado,
                           String metodoPago, List<OrdenDetalleDto> detalles) {}

    // ---- Biblioteca ----
    @Schema(description = "Juego perteneciente a la biblioteca del usuario.")
    public record BibliotecaItemDto(Integer juegoId, String titulo, String imagenUrl,
                                    boolean requiereCloud, LocalDateTime fechaAdquisicion) {}

    // ---- Resenas ----
    @Schema(description = "Reseña de un juego adquirido por el usuario.")
    public record ResenaRequest(
            @Min(1) @Max(5) int calificacion,
            @Size(max = 500) String comentario) {}

    // ---- Historial / recomendaciones ----
    @Schema(description = "Interacción de vista registrada en el historial del usuario.")
    public record HistorialRequest(
            @NotNull @Positive Integer juegoId,
            @Min(0) @Max(86400) int segundos,
            @NotNull @Pattern(regexp = "CATALOGO|BUSQUEDA|BIBLIOTECA|RECOMENDACION",
                    message = "origen debe ser CATALOGO, BUSQUEDA, BIBLIOTECA o RECOMENDACION") String origen) {}

    @Schema(description = "Juego recomendado con puntaje y origen de recomendación.")
    public record RecomendacionDto(CatalogoDtos.JuegoResumenDto juego, BigDecimal puntaje, String origen) {}
}
