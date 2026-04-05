package com.tickets.user_service.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("Usuario no encontrado con id: " + id);
    }

    public UserNotFoundException(String email) {
        super("Usuario no encontrado con email: " + email);
    }
}
