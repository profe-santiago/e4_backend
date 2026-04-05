package com.tickets.ticket_service.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * event-service publica esto cuando no hay stock suficiente.
 * Alias RabbitMQ: "StockFailedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockFailedEvent {
    private UUID orderId;
    private String reason;
}
