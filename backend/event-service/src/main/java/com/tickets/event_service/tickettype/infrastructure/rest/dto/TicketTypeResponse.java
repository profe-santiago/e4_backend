package com.tickets.event_service.tickettype.infrastructure.rest.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class TicketTypeResponse {

    private Long id;
    private UUID eventId;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private int totalQuantity;
    private int availableQuantity;
}
