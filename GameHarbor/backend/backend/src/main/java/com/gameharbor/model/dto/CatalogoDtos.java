package com.gameharbor.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

public final class CatalogoDtos {

    private CatalogoDtos() {}

    @Schema(description = "Categoría del catálogo.")
    public record CategoriaDto(Integer id, String nombre, String descripcion) {}

    @Schema(description = "Método de pago activo.")
    public record MetodoPagoDto(Integer id, String nombre) {}

    @Schema(description = "Información resumida de un juego del catálogo.")
    public record JuegoResumenDto(
            Integer id,
            String titulo,
            BigDecimal precio,
            String imagenUrl,
            boolean requiereCloud,
            String desarrollador,
            List<String> categorias) {}

    @Schema(description = "Detalle de un juego y sus calificaciones agregadas.")
    public record JuegoDetalleDto(
            Integer id,
            String titulo,
            String descripcion,
            BigDecimal precio,
            LocalDate fechaLanzamiento,
            String imagenUrl,
            boolean requiereCloud,
            String estado,
            String desarrollador,
            List<String> categorias,
            Double calificacionPromedio,
            long totalResenas) {}

    @Schema(description = "Reseña pública de un juego.")
    public record ResenaDto(Integer id, String usuario, int calificacion, String comentario, LocalDateTime fecha) {}

    /** Pagina simple: evita serializar PageImpl directamente. */
    @Schema(description = "Página de resultados del catálogo.")
    public record PageDto<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {}
}
