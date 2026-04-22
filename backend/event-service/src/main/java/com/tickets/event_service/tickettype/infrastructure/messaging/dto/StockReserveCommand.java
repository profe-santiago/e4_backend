package com.tickets.event_service.tickettype.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO de mensajería — representa el comando recibido desde ticket-service.
 * Vive en infrastructure/messaging — el dominio nunca lo ve directamente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReserveCommand {
    private UUID orderId;
    private UUID userId;
    private List<StockReserveItem> items;
}
