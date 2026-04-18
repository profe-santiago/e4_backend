package com.tickets.payment_service.payment.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Evento publicado por payment-service cuando el reembolso en Stripe fue exitoso.
 * ticket-service consume esto para marcar la orden como REFUNDED.
 * Type alias RabbitMQ: "RefundCompletedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundCompletedEvent {
    private UUID orderId;
    private UUID userId;
}
