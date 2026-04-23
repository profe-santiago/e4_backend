package com.tickets.ticket_service.exception;

import com.tickets.ticket_service.ticket.domain.TicketStatus;

public class InvalidTicketException extends RuntimeException {

    public InvalidTicketException(TicketStatus status) {
        super("El ticket no es valido para el ingreso. Estado actual: " + status);
    }
}
