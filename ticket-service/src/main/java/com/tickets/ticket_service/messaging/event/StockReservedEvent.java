package com.tickets.ticket_service.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * event-service publica esto cuando el stock fue reservado exitosamente.
 * Incluye el precio real de cada ticket type para que ticket-service confirme la orden.
 * Alias RabbitMQ: "StockReservedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservedEvent {
    private UUID orderId;
    private List<StockReservedItem> items;
}
