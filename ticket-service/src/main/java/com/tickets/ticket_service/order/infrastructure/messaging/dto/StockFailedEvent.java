package com.tickets.ticket_service.order.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * event-service publica esto cuando no hay stock suficiente.
 * Type alias RabbitMQ: "StockFailedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockFailedEvent {
    private UUID orderId;
    private String reason;
}
