package com.tickets.ticket_service.exception;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(UUID id) {
        super("Orden no encontrada con id: " + id);
    }
}
