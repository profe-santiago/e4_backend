package com.tickets.payment_service.payment.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Evento publicado hacia ticket-service cuando Stripe confirma el cobro.
 * Routing key: payment.completed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    private UUID orderId;
    private UUID userId;
    private UUID paymentId;
    private String stripePaymentIntentId;
}
