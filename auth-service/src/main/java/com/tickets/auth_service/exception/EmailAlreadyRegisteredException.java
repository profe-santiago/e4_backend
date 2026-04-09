package com.tickets.auth_service.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(String email) {
        super("El email ya está registrado: " + email);
    }
}
