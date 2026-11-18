package com.gameharbor.controller;

import com.gameharbor.config.AuthUser;
import com.gameharbor.model.dto.TiendaDtos.BibliotecaItemDto;
import com.gameharbor.model.dto.TiendaDtos.CompraRequest;
import com.gameharbor.model.dto.TiendaDtos.OrdenDto;
import com.gameharbor.service.OrdenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrdenController {

    private final OrdenService ordenes;

    public OrdenController(OrdenService ordenes) {
        this.ordenes = ordenes;
    }

    /** Paga todo el carrito actual. */
    @PostMapping("/ordenes")
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenDto comprar(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CompraRequest req) {
        return ordenes.comprar(user.id(), req.metodoPagoId());
    }

    @GetMapping("/ordenes")
    public List<OrdenDto> historial(@AuthenticationPrincipal AuthUser user) {
        return ordenes.historial(user.id());
    }

    @GetMapping("/biblioteca")
    public List<BibliotecaItemDto> biblioteca(@AuthenticationPrincipal AuthUser user) {
        return ordenes.biblioteca(user.id());
    }
}
