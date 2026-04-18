package com.tickets.ticket_service.order.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * ticket-service publica esto cuando el usuario solicita un reembolso.
 * payment-service consume esto para procesar el reembolso en Stripe.
 * Type alias RabbitMQ: "RefundInitiatedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundInitiatedEvent {
    private UUID orderId;
    private UUID userId;
}
