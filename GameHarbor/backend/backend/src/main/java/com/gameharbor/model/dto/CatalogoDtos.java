package com.gameharbor.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class CatalogoDtos {

    private CatalogoDtos() {}

    public record CategoriaDto(Integer id, String nombre, String descripcion) {}

    public record MetodoPagoDto(Integer id, String nombre) {}

    public record JuegoResumenDto(
            Integer id,
            String titulo,
            BigDecimal precio,
            String imagenUrl,
            boolean requiereCloud,
            String desarrollador,
            List<String> categorias) {}

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

    public record ResenaDto(Integer id, String usuario, int calificacion, String comentario, LocalDateTime fecha) {}

    /** Pagina simple: evita serializar PageImpl directamente. */
    public record PageDto<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {}
}
