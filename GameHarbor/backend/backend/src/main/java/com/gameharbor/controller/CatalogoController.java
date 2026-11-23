package com.gameharbor.controller;

import com.gameharbor.config.AuthUser;
import com.gameharbor.model.dto.CatalogoDtos.*;
import com.gameharbor.model.dto.TiendaDtos.ResenaRequest;
import com.gameharbor.service.CatalogoService;
import com.gameharbor.service.ResenaService;
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
    public PageDto<JuegoResumenDto> buscar(@Size(max = 150) @RequestParam(required = false) String q,
                                           @Positive @RequestParam(required = false) Integer categoriaId,
                                           @Min(0) @RequestParam(defaultValue = "0") int pagina,
                                           @Min(1) @Max(50) @RequestParam(defaultValue = "12") int tamano) {
        return catalogo.buscar(q, categoriaId, pagina, tamano);
    }

    @GetMapping("/juegos/{id}")
    public JuegoDetalleDto detalle(@Positive @PathVariable Integer id) {
        return catalogo.detalle(id);
    }

    @GetMapping("/juegos/{id}/resenas")
    public List<ResenaDto> resenas(@Positive @PathVariable Integer id) {
        return catalogo.resenas(id);
    }

    @PostMapping("/juegos/{id}/resenas")
    public ResenaDto resenar(@AuthenticationPrincipal AuthUser user, @Positive @PathVariable Integer id,
                             @Valid @RequestBody ResenaRequest req) {
        return resenas.agregarOActualizar(user.id(), id, req);
    }

    @GetMapping("/categorias")
    public List<CategoriaDto> categorias() {
        return catalogo.categorias();
    }

    @GetMapping("/metodos-pago")
    public List<MetodoPagoDto> metodosPago() {
        return catalogo.metodosPago();
    }
}
