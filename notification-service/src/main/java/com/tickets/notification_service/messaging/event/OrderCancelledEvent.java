package com.tickets.notification_service.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Espejo del evento publicado por ticket-service cuando una orden es cancelada o fallida.
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
