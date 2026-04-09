package com.tickets.payment_service.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String identifier) {
        super("Pago no encontrado: " + identifier);
    }
}
