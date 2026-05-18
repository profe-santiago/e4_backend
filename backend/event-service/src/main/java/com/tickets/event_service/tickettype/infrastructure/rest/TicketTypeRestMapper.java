package com.tickets.event_service.tickettype.infrastructure.rest;

import com.tickets.event_service.tickettype.application.dto.CreateTicketTypeCommand;
import com.tickets.event_service.tickettype.application.dto.UpdateTicketTypeCommand;
import com.tickets.event_service.tickettype.domain.Money;
import com.tickets.event_service.tickettype.domain.TicketType;
import com.tickets.event_service.tickettype.infrastructure.rest.dto.TicketTypeRequest;
import com.tickets.event_service.tickettype.infrastructure.rest.dto.TicketTypeResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper REST para TicketType: HTTP request ↔ Command + Domain → HTTP response.
 */
@Component
public class TicketTypeRestMapper {

    public CreateTicketTypeCommand toCreateCommand(UUID eventId, TicketTypeRequest request,
                                                    UUID requesterId, boolean isAdmin) {
        String currency = request.getCurrency() != null ? request.getCurrency() : "USD";
        return new CreateTicketTypeCommand(
                eventId, requesterId, isAdmin,
                request.getName(),
                request.getDescription(),
                Money.of(request.getPrice(), currency),
                request.getTotalQuantity(),
                request.getSaleStartDate(),
                request.getSaleEndDate()
        );
    }

    public UpdateTicketTypeCommand toUpdateCommand(TicketTypeRequest request,
                                                    UUID requesterId, boolean isAdmin) {
        String currency = request.getCurrency() != null ? request.getCurrency() : "USD";
        return new UpdateTicketTypeCommand(
                requesterId, isAdmin,
                request.getName(),
                request.getDescription(),
                Money.of(request.getPrice(), currency),
                request.getTotalQuantity(),
                request.getSaleStartDate(),
                request.getSaleEndDate()
        );
    }

    public TicketTypeResponse toResponse(TicketType ticketType) {
        return TicketTypeResponse.builder()
                .id(ticketType.getId())
                .eventId(ticketType.getEventId())
                .name(ticketType.getName())
                .description(ticketType.getDescription())
                .price(ticketType.getPrice().amount())
                .currency(ticketType.getPrice().currency())
                .totalQuantity(ticketType.getTotalQuantity())
                .availableQuantity(ticketType.getAvailableQuantity())
                .saleStartDate(ticketType.getSaleStartDate())
                .saleEndDate(ticketType.getSaleEndDate())
                .build();
    }
}
