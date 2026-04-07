package com.tickets.ticket_service.ticket.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Command de entrada para generar tickets de una orden confirmada.
 * Desacopla GenerateTicketsUseCase de la entidad de dominio Order —
 * el UseCase nunca importa Order ni OrderItem de otro bounded context.
 */
public record GenerateTicketsCommand(
        UUID orderId,
        UUID userId,
        List<OrderItemData> items
) {
    public record OrderItemData(Long orderItemId, UUID eventId, Long ticketTypeId, int quantity) {}
}
