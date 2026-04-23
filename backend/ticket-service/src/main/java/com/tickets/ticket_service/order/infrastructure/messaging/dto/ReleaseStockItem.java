package com.tickets.ticket_service.order.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Ítem dentro de OrderCancelledEvent / OrderRefundedEvent.
 * Indica a event-service qué stock debe restituirse.
 * Type alias RabbitMQ: "ReleaseStockItem"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseStockItem {
    private UUID eventId;
    private Long ticketTypeId;
    private int quantity;
}
