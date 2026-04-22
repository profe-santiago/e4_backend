package com.tickets.event_service.tickettype.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Evento publicado por ticket-service cuando una orden es reembolsada.
 * event-service consume esto para liberar el stock reservado.
 * Type alias RabbitMQ: "OrderRefundedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundedEvent {
    private UUID orderId;
    private UUID userId;
    private List<ReleaseStockItem> items;
}
