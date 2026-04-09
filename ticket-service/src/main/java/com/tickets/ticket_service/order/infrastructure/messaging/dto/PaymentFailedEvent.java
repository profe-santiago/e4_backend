package com.tickets.ticket_service.order.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * payment-service publica esto cuando el cobro falla.
 * Type alias RabbitMQ: "PaymentFailedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private UUID orderId;
    private String reason;
}
