package com.tickets.ticket_service.ticket;

import com.tickets.ticket_service.ticket.dto.TicketResponse;

public final class TicketMapper {

    private TicketMapper() {}

    public static TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .orderId(ticket.getOrderItem().getOrder().getId())
                .userId(ticket.getUserId())
                .eventId(ticket.getEventId())
                .ticketTypeId(ticket.getTicketTypeId())
                .qrCode(ticket.getQrCode())
                .status(ticket.getStatus())
                .purchasedAt(ticket.getPurchasedAt())
                .usedAt(ticket.getUsedAt())
                .build();
    }
}
