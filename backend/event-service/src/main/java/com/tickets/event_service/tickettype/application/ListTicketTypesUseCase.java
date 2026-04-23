package com.tickets.event_service.tickettype.application;

import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.shared.UseCase;
import com.tickets.event_service.tickettype.domain.TicketType;
import com.tickets.event_service.tickettype.domain.TicketTypeRepository;

import java.util.List;
import java.util.UUID;

@UseCase
public class ListTicketTypesUseCase {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    public ListTicketTypesUseCase(EventRepository eventRepository,
                                   TicketTypeRepository ticketTypeRepository) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    public List<TicketType> execute(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException(eventId);
        }
        return ticketTypeRepository.findAllByEventId(eventId);
    }
}
