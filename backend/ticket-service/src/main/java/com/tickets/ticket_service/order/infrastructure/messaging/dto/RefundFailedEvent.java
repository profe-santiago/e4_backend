package com.tickets.ticket_service.order.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * payment-service publica esto cuando el reembolso en Stripe falló.
 * ticket-service consume esto para revertir la orden de REFUND_PENDING a CONFIRMED.
 * Type alias RabbitMQ: "RefundFailedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundFailedEvent {
    private UUID orderId;
    private UUID userId;
    private String reason;
}
