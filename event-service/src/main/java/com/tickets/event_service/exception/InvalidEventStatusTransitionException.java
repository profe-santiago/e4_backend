package com.tickets.event_service.exception;

import com.tickets.event_service.event.EventStatus;

public class InvalidEventStatusTransitionException extends RuntimeException {
    public InvalidEventStatusTransitionException(EventStatus from, EventStatus to) {
        super("Transición de estado inválida: " + from + " → " + to);
    }
}
