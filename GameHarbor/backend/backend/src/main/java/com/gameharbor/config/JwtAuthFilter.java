package com.gameharbor.config;

import com.gameharbor.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            jwtUtil.validar(header.substring(7)).ifPresent(this::autenticar);
        }
        chain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private void autenticar(Claims claims) {
        Integer uid = claims.get("uid", Integer.class);
        List<String> roles = claims.get("roles", List.class);
        if (uid == null) return;

        var authorities = (roles == null ? List.<String>of() : roles).stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
        var auth = new UsernamePasswordAuthenticationToken(new AuthUser(uid, claims.getSubject()), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
