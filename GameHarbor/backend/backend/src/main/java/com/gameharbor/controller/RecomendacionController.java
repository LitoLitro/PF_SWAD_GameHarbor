package com.gameharbor.controller;

import com.gameharbor.config.AuthUser;
import com.gameharbor.model.dto.TiendaDtos.HistorialRequest;
import com.gameharbor.model.dto.TiendaDtos.RecomendacionDto;
import com.gameharbor.service.RecomendacionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api")
public class RecomendacionController {

    private final RecomendacionService recomendaciones;

    public RecomendacionController(RecomendacionService recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    @GetMapping("/recomendaciones")
    public List<RecomendacionDto> recomendaciones(@AuthenticationPrincipal AuthUser user,
                                                  @Min(1) @Max(30) @RequestParam(defaultValue = "10") int limite) {
        return recomendaciones.para(user.id(), limite);
    }

    /** El frontend avisa cuando el usuario ve un juego; alimenta el modelo de ML. */
    @PostMapping("/historial")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrarVista(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody HistorialRequest req) {
        recomendaciones.registrarVista(user.id(), req);
    }
}
