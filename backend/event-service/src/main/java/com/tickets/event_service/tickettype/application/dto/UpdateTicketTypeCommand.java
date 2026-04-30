package com.tickets.event_service.tickettype.application.dto;

import com.tickets.event_service.tickettype.domain.Money;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateTicketTypeCommand(
        UUID requesterId,
        boolean isAdmin,
        String name,
        String description,
        Money price,
        int totalQuantity,
        LocalDateTime saleStartDate,
        LocalDateTime saleEndDate
) {}
