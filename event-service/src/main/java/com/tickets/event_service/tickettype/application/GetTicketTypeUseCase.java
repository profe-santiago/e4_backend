package com.tickets.event_service.tickettype.application;

import com.tickets.event_service.exception.TicketTypeNotFoundException;
import com.tickets.event_service.shared.UseCase;
import com.tickets.event_service.tickettype.domain.TicketType;
import com.tickets.event_service.tickettype.domain.TicketTypeRepository;

import java.util.UUID;

@UseCase
public class GetTicketTypeUseCase {

    private final TicketTypeRepository ticketTypeRepository;

    public GetTicketTypeUseCase(TicketTypeRepository ticketTypeRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
    }

    public TicketType execute(UUID eventId, Long id) {
        return ticketTypeRepository.findByIdAndEventId(id, eventId)
                .orElseThrow(() -> new TicketTypeNotFoundException(id));
    }
}
