package com.tickets.ticket_service.exception;

public class TicketAlreadyUsedException extends RuntimeException {

    public TicketAlreadyUsedException() {
        super("No se puede solicitar el reembolso porque uno o más boletos ya fueron utilizados o el evento ya ocurrió");
    }
}
