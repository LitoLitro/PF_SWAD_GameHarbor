package com.gameharbor.service;

import com.gameharbor.exception.ApiException;
import com.gameharbor.model.dto.AuthDtos.*;
import com.gameharbor.model.entity.Carrito;
import com.gameharbor.model.entity.Rol;
import com.gameharbor.model.entity.Usuario;
import com.gameharbor.repository.CarritoRepository;
import com.gameharbor.repository.RolRepository;
import com.gameharbor.repository.UsuarioRepository;
import com.gameharbor.util.JwtUtil;
import com.gameharbor.util.Mapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String ROL_POR_DEFECTO = "CLIENTE";

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final CarritoRepository carritos;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthService(UsuarioRepository usuarios, RolRepository roles, CarritoRepository carritos,
                       PasswordEncoder encoder, JwtUtil jwt) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.carritos = carritos;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** Equivale a SP_REGISTRAR_USUARIO: crea usuario + rol CLIENTE + carrito, pero con hash BCrypt. */
    @Transactional
    public AuthResponse registrar(RegisterRequest req) {
        String email = normalizar(req.email());
        if (usuarios.existsByEmail(email)) {
            throw ApiException.conflict("El correo ya está registrado");
        }
        Rol cliente = roles.findByNombre(ROL_POR_DEFECTO)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Falta el rol " + ROL_POR_DEFECTO + " en la base de datos (ejecuta GameHarborInserts.sql)"));

        Usuario u = new Usuario();
        u.setNombre(req.nombre().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        u.getRoles().add(cliente);
        usuarios.save(u);

        Carrito carrito = new Carrito();
        carrito.setUsuario(u);
        carritos.save(carrito);

        return respuesta(u);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarios.findByEmail(normalizar(req.email()))
                .filter(x -> "ACTIVO".equals(x.getEstado()))
                .filter(x -> encoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Credenciales inválidas o usuario inactivo"));
        return respuesta(u);
    }

    private AuthResponse respuesta(Usuario u) {
        UsuarioDto dto = Mapper.usuario(u);
        return new AuthResponse(jwt.generar(u.getId(), u.getEmail(), dto.roles()), dto);
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
