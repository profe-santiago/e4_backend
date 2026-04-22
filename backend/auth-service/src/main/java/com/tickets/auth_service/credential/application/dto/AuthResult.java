package com.tickets.auth_service.credential.application.dto;

/**
 * Resultado de un caso de uso de autenticación.
 * Value object inmutable — sale de la capa de aplicación hacia el adaptador REST.
 */
public record AuthResult(String token, String role, String email) {}
