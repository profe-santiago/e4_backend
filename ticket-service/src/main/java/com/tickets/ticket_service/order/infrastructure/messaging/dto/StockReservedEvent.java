package com.tickets.ticket_service.order.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * event-service publica esto cuando el stock fue reservado exitosamente.
 * Incluye el precio real de cada ticket type.
 * Type alias RabbitMQ: "StockReservedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservedEvent {
    private UUID orderId;
    private List<StockReservedItem> items;
}
