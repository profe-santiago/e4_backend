package com.tickets.auth_service.exception;

/**
 * No revela si el email existe o no — mensaje genérico intencional.
 * Previene enumeración de usuarios.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
}
