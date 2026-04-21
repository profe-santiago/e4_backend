package com.tickets.payment_service.payment.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Publicado por payment-service cuando Stripe rechaza el reembolso.
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
