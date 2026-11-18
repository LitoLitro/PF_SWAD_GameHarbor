package com.gameharbor.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(max = 80) String nombre,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {}

    public record UsuarioDto(Integer id, String nombre, String email, BigDecimal saldo, List<String> roles) {}

    public record AuthResponse(String token, UsuarioDto usuario) {}
}
