package com.tickets.ticket_service.order.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * payment-service publica esto cuando el reembolso en Stripe fue exitoso.
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
