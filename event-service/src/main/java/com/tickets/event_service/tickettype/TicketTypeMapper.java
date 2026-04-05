package com.tickets.event_service.tickettype;

import com.tickets.event_service.tickettype.dto.TicketTypeResponse;

/**
 * Mapper estático para TicketType.
 */
public final class TicketTypeMapper {

    private TicketTypeMapper() {}

    public static TicketTypeResponse toResponse(TicketType ticketType) {
        return TicketTypeResponse.builder()
                .id(ticketType.getId())
                .eventId(ticketType.getEvent().getId())
                .name(ticketType.getName())
                .description(ticketType.getDescription())
                .price(ticketType.getPrice())
                .totalQuantity(ticketType.getTotalQuantity())
                .availableQuantity(ticketType.getAvailableQuantity())
                .build();
    }
}
