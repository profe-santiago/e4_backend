package com.tickets.ticket_service.order.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Command de entrada para crear una orden.
 * El controller extrae userId de Authentication antes de construir este command.
 */
public record CreateOrderCommand(
        UUID userId,
        String paymentIntentId,
        List<OrderItemData> items
) {
    public record OrderItemData(UUID eventId, Long ticketTypeId, int quantity) {}
}
