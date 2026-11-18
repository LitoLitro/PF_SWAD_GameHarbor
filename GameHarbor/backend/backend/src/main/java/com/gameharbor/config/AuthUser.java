package com.gameharbor.config;

/** Usuario autenticado que viaja en el SecurityContext (se obtiene con @AuthenticationPrincipal). */
public record AuthUser(Integer id, String email) {}
