package com.tickets.event_service.exception;

import com.tickets.event_service.event.domain.EventStatus;

public class EventNotDeletableException extends RuntimeException {
    public EventNotDeletableException(EventStatus status) {
        super("No se puede eliminar un evento en estado " + status + ". Solo se pueden eliminar eventos en estado DRAFT.");
    }
}
