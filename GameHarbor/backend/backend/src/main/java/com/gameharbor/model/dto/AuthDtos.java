package com.gameharbor.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

public final class AuthDtos {

    private AuthDtos() {}

    @Schema(description = "Datos para registrar un usuario cliente.")
    public record RegisterRequest(
            @NotBlank @Size(max = 80) String nombre,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password) {}

    @Schema(description = "Credenciales de inicio de sesión.")
    public record LoginRequest(
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(max = 72) String password) {}

    @Schema(description = "Perfil básico del usuario y sus roles.")
    public record UsuarioDto(Integer id, String nombre, String email, BigDecimal saldo, List<String> roles) {}

    @Schema(description = "Respuesta de autenticación con JWT y datos del usuario.")
    public record AuthResponse(String token, UsuarioDto usuario) {}
}
