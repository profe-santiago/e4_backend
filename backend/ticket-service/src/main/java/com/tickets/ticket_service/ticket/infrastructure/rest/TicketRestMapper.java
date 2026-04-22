package com.tickets.ticket_service.ticket.infrastructure.rest;

import com.tickets.ticket_service.ticket.domain.Ticket;
import com.tickets.ticket_service.ticket.infrastructure.rest.dto.TicketResponse;
import org.springframework.stereotype.Component;

@Component
public class TicketRestMapper {

    public TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .orderId(ticket.getOrderId())
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
