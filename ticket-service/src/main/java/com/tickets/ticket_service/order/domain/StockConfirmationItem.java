package com.tickets.ticket_service.order.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Value Object que representa la confirmación de stock de un ítem.
 * Usado por Order.confirm() — viene de event-service via RabbitMQ, pero el dominio
 * solo ve este record limpio, nunca el DTO de mensajería.
 */
public record StockConfirmationItem(
        UUID eventId,
        Long ticketTypeId,
        int quantity,
        BigDecimal unitPrice
) {}
