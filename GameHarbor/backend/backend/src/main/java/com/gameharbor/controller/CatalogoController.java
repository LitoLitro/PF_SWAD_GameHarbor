package com.gameharbor.controller;

import com.gameharbor.config.AuthUser;
import com.gameharbor.model.dto.CatalogoDtos.*;
import com.gameharbor.model.dto.TiendaDtos.ResenaRequest;
import com.gameharbor.service.CatalogoService;
import com.gameharbor.service.ResenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@Tag(name = "Catálogo", description = "Consulta pública de juegos, reseñas, categorías y métodos de pago.")
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogo;
    private final ResenaService resenas;

    public CatalogoController(CatalogoService catalogo, ResenaService resenas) {
        this.catalogo = catalogo;
        this.resenas = resenas;
    }

    /** GET /api/juegos?q=texto&categoriaId=3&pagina=0&tamano=12 */
    @GetMapping("/juegos")
    @Operation(summary = "Buscar juegos", description = "Devuelve una página de juegos disponibles; admite texto, categoría y paginación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de resultados", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda o paginación inválidos")
    })
    public PageDto<JuegoResumenDto> buscar(
            @Parameter(description = "Búsqueda por título (máximo 150 caracteres)")
            @Size(max = 150) @RequestParam(required = false) String q,
            @Parameter(description = "ID positivo de categoría")
            @Positive @RequestParam(required = false) Integer categoriaId,
            @Parameter(description = "Índice de página basado en cero", example = "0")
            @Min(0) @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Cantidad de resultados por página (1–50)", example = "12")
            @Min(1) @Max(50) @RequestParam(defaultValue = "12") int tamano) {
        return catalogo.buscar(q, categoriaId, pagina, tamano);
    }

    @GetMapping("/juegos/{id}")
    @Operation(summary = "Ver detalle de juego", description = "Devuelve el detalle y el promedio de reseñas de un juego.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle devuelto", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Identificador inválido"),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado")
    })
    public JuegoDetalleDto detalle(@Parameter(description = "ID positivo del juego")
                                   @Positive @PathVariable Integer id) {
        return catalogo.detalle(id);
    }

    @GetMapping("/juegos/{id}/resenas")
    @Operation(summary = "Listar reseñas", description = "Devuelve las reseñas públicas del juego.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseñas devueltas", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Identificador inválido"),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado")
    })
    public List<ResenaDto> resenas(@Parameter(description = "ID positivo del juego")
                                   @Positive @PathVariable Integer id) {
        return catalogo.resenas(id);
    }

    @PostMapping("/juegos/{id}/resenas")
    @Operation(summary = "Crear o actualizar reseña", description = "Requiere JWT y que el usuario tenga el juego en su biblioteca.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseña guardada", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Identificador o contenido inválido"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación JWT"),
            @ApiResponse(responseCode = "403", description = "El usuario no posee el juego"),
            @ApiResponse(responseCode = "404", description = "Juego no encontrado")
    })
    public ResenaDto resenar(@AuthenticationPrincipal AuthUser user,
                             @Parameter(description = "ID positivo del juego") @Positive @PathVariable Integer id,
                             @Valid @RequestBody ResenaRequest req) {
        return resenas.agregarOActualizar(user.id(), id, req);
    }

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorías", description = "Devuelve las categorías disponibles para filtrar el catálogo.")
    public List<CategoriaDto> categorias() {
        return catalogo.categorias();
    }

    @GetMapping("/metodos-pago")
    @Operation(summary = "Listar métodos de pago", description = "Devuelve los métodos de pago activos.")
    public List<MetodoPagoDto> metodosPago() {
        return catalogo.metodosPago();
    }
}
