package com.tickets.ticket_service.order.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * ticket-service publica esto cuando la orden transicionó a REFUNDED.
 * event-service consume esto para restituir el stock.
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
