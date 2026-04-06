package com.tickets.event_service.tickettype.application.dto;

import com.tickets.event_service.tickettype.domain.Money;

import java.util.UUID;

public record CreateTicketTypeCommand(
        UUID eventId,
        UUID requesterId,
        boolean isAdmin,
        String name,
        String description,
        Money price,
        int totalQuantity
) {}
