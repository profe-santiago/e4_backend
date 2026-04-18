package com.tickets.payment_service.payment.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Evento publicado por ticket-service cuando el usuario solicita un reembolso.
 * payment-service consume esto para procesar el refund en Stripe.
 * Type alias RabbitMQ: "RefundInitiatedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundInitiatedEvent {
    private UUID orderId;
    private UUID userId;
}
