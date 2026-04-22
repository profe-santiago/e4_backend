package com.tickets.payment_service.payment.domain;

/**
 * Value Object que representa el resultado de intentar cobrar a un gateway de pago.
 *
 * El dominio define QUÉ necesita saber del resultado; la infraestructura
 * (StripePaymentGateway) mapea la respuesta del SDK a este tipo.
 * El dominio nunca ve StripeException ni PaymentIntent.
 */
public record PaymentChargeResult(boolean succeeded, String transactionId, String failureReason) {

    public static PaymentChargeResult success(String transactionId) {
        return new PaymentChargeResult(true, transactionId, null);
    }

    public static PaymentChargeResult failure(String reason) {
        return new PaymentChargeResult(false, null, reason);
    }
}
