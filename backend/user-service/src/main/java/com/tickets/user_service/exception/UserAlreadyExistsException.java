package com.tickets.user_service.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String email) {
        super("Ya existe un perfil registrado con el email: " + email);
    }
}
