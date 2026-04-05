package com.tickets.ticket_service.ticket.dto;

import com.tickets.ticket_service.ticket.TicketStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TicketResponse {
    private UUID id;
    private UUID orderId;
    private UUID userId;
    private UUID eventId;
    private Long ticketTypeId;
    private String qrCode;
    private TicketStatus status;
    private LocalDateTime purchasedAt;
    private LocalDateTime usedAt;
}
