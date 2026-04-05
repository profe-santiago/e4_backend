package com.tickets.event_service.exception;

public class TicketTypeNotFoundException extends RuntimeException {
    public TicketTypeNotFoundException(Long id) {
        super("Tipo de ticket no encontrado con id: " + id);
    }
}
