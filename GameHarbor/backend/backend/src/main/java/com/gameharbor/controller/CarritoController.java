package com.gameharbor.controller;

import com.gameharbor.config.AuthUser;
import com.gameharbor.model.dto.TiendaDtos.AgregarCarritoRequest;
import com.gameharbor.model.dto.TiendaDtos.CarritoDto;
import com.gameharbor.service.CarritoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carrito;

    public CarritoController(CarritoService carrito) {
        this.carrito = carrito;
    }

    @GetMapping
    public CarritoDto ver(@AuthenticationPrincipal AuthUser user) {
        return carrito.obtener(user.id());
    }

    @PostMapping("/items")
    public CarritoDto agregar(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody AgregarCarritoRequest req) {
        return carrito.agregar(user.id(), req.juegoId());
    }

    @DeleteMapping("/items/{juegoId}")
    public CarritoDto quitar(@AuthenticationPrincipal AuthUser user, @PathVariable Integer juegoId) {
        return carrito.quitar(user.id(), juegoId);
    }

    @DeleteMapping
    public CarritoDto vaciar(@AuthenticationPrincipal AuthUser user) {
        return carrito.vaciar(user.id());
    }
}
