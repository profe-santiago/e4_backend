package com.tickets.notification_service.notification.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de mensajería — espejo del evento publicado por ticket-service.
 * Alias RabbitMQ: "OrderCancelledEvent" — debe coincidir con el publisher.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {

    private UUID orderId;
    private UUID userId;
    private String reason;
}
