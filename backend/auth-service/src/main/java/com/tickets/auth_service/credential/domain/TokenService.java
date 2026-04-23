package com.tickets.auth_service.credential.domain;

import java.util.UUID;

/**
 * Puerto secundario (salida) para la generación de tokens de autenticación.
 * El dominio define el contrato — la infraestructura elige JWT, OAuth2, etc.
 */
public interface TokenService {

    String generate(UUID userId, String email, String role);
}
