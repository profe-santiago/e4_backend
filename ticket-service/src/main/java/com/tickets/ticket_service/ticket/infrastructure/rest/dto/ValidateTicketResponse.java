package com.tickets.ticket_service.ticket.infrastructure.rest.dto;

import com.tickets.ticket_service.ticket.domain.TicketStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ValidateTicketResponse {
    private UUID ticketId;
    private UUID eventId;
    private Long ticketTypeId;
    private UUID userId;
    private TicketStatus status;
    private String message;
}
