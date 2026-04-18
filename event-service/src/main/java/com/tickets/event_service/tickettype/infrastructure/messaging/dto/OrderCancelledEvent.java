package com.tickets.event_service.tickettype.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Evento publicado por ticket-service cuando una orden es cancelada o falla.
 * event-service consume esto para liberar el stock reservado.
 * Type alias RabbitMQ: "OrderCancelledEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {
    private UUID orderId;
    private UUID userId;
    private String reason;
    private List<ReleaseStockItem> items;
}
