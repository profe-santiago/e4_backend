package com.tickets.event_service.tickettype.domain;

public class SaleNotAvailableException extends RuntimeException {
    public SaleNotAvailableException(String message) {
        super(message);
    }
}
