package com.gameharbor.controller;

import com.gameharbor.model.dto.AuthDtos.AuthResponse;
import com.gameharbor.model.dto.AuthDtos.LoginRequest;
import com.gameharbor.model.dto.AuthDtos.RegisterRequest;
import com.gameharbor.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return auth.registrar(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }
}
