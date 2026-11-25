package com.gameharbor.controller;

import com.gameharbor.model.dto.AuthDtos.AuthResponse;
import com.gameharbor.model.dto.AuthDtos.LoginRequest;
import com.gameharbor.model.dto.AuthDtos.RegisterRequest;
import com.gameharbor.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Autenticación", description = "Registro de cuentas e inicio de sesión mediante JWT.")
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar usuario", description = "Crea una cuenta con rol CLIENTE y devuelve un JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "El correo ya está registrado")
    })
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return auth.registrar(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida las credenciales y devuelve un JWT vigente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación correcta", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas o cuenta inactiva")
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }
}
