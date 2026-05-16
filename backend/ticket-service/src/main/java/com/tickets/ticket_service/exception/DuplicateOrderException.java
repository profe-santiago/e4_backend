package com.tickets.ticket_service.exception;

public class DuplicateOrderException extends RuntimeException {

    public DuplicateOrderException(String paymentIntentId) {
        super("Ya existe una orden para el intento de pago: " + paymentIntentId);
    }
}
