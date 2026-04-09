package com.tickets.ticket_service.order.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * ticket-service publica esto cuando una orden es cancelada o falla.
 * event-service consume esto para liberar el stock.
 * Type alias RabbitMQ: "OrderCancelledEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {
    private UUID orderId;
    private UUID userId;
    private String reason;
}
