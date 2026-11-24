package com.gameharbor.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GameHarbor API",
                version = "1.0.0",
                description = "API REST para catálogo, compras digitales, biblioteca y sesiones de cloud gaming simuladas."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Introduce el JWT devuelto por el inicio de sesión o el registro."
)
public class OpenApiConfig {
}
