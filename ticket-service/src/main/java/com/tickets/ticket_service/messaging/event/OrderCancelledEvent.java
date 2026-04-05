package com.tickets.ticket_service.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * ticket-service publica esto cuando una orden queda en FAILED o CANCELLED.
 * notification-service puede usar esto para avisar al usuario.
 * Alias RabbitMQ: "OrderCancelledEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {
    private UUID orderId;
    private UUID userId;
    private String reason;
}
