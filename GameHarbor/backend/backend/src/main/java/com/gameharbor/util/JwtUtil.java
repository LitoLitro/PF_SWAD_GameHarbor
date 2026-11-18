package com.gameharbor.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos 32 caracteres");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMinutes = expirationMinutes;
    }

    public String generar(Integer usuarioId, String email, List<String> roles) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("uid", usuarioId)
                .claim("roles", roles)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(expirationMinutes * 60)))
                .signWith(key)
                .compact();
    }

    /** Devuelve los claims si el token es valido y no ha expirado. */
    public Optional<Claims> validar(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
