package com.gameharbor.controller;

import com.gameharbor.config.AuthUser;
import com.gameharbor.model.dto.CloudDtos.FinalizarSesionRequest;
import com.gameharbor.model.dto.CloudDtos.IniciarSesionRequest;
import com.gameharbor.model.dto.CloudDtos.SesionDto;
import com.gameharbor.service.CloudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cloud/sesiones")
public class CloudController {

    private final CloudService cloud;

    public CloudController(CloudService cloud) {
        this.cloud = cloud;
    }

    /** "Jugar" un juego de la biblioteca: abre una sesion en un servidor cloud simulado. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SesionDto iniciar(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody IniciarSesionRequest req) {
        return cloud.iniciar(user.id(), req.juegoId());
    }

    @PostMapping("/{id}/finalizar")
    public SesionDto finalizar(@AuthenticationPrincipal AuthUser user, @PathVariable Integer id,
                               @Valid @RequestBody(required = false) FinalizarSesionRequest req) {
        return cloud.finalizar(user.id(), id, req);
    }

    @GetMapping("/activas")
    public List<SesionDto> activas(@AuthenticationPrincipal AuthUser user) {
        return cloud.activas(user.id());
    }
}
