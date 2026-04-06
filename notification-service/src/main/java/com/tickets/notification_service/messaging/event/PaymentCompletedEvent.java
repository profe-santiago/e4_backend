package com.tickets.notification_service.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Espejo del evento publicado por payment-service cuando Stripe confirma el cobro.
 * Alias RabbitMQ: "PaymentCompletedEvent" — debe coincidir con el publisher.
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
