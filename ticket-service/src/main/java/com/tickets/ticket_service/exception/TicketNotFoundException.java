package com.tickets.ticket_service.exception;

import java.util.UUID;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(UUID id) {
        super("Ticket no encontrado con id: " + id);
    }
}
