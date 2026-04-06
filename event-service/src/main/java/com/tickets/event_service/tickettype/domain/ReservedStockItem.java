package com.tickets.event_service.tickettype.domain;

import java.util.UUID;

/**
 * Value Object que representa un item de stock reservado exitosamente.
 * Usado por StockEventPublisher para comunicar el resultado al ticket-service.
 */
public record ReservedStockItem(
        UUID eventId,
        Long ticketTypeId,
        int quantity,
        Money unitPrice
) {}
