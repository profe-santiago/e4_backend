package com.tickets.notification_service.notification.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de mensajeria — espejo del evento publicado por payment-service.
 * Alias RabbitMQ: "RefundFailedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundFailedEvent {
    private UUID orderId;
    private UUID userId;
    private String reason;
}
