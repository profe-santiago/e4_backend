package com.tickets.ticket_service.order.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Comando que ticket-service publica para que event-service reserve stock.
 * Type alias RabbitMQ: "StockReserveCommand"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReserveCommand {
    private UUID orderId;
    private UUID userId;
    private List<StockReserveItem> items;
}
