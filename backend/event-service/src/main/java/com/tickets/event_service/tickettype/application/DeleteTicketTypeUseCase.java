package com.tickets.event_service.tickettype.application;

import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.exception.TicketTypeNotFoundException;
import com.tickets.event_service.exception.UnauthorizedActionException;
import com.tickets.event_service.shared.UseCase;
import com.tickets.event_service.tickettype.domain.TicketTypeRepository;

import java.util.UUID;

@UseCase
public class DeleteTicketTypeUseCase {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    public DeleteTicketTypeUseCase(EventRepository eventRepository,
                                    TicketTypeRepository ticketTypeRepository) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    public void execute(UUID eventId, Long ticketTypeId, UUID requesterId, boolean isAdmin) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!isAdmin && !event.getOrganizerId().equals(requesterId)) {
            throw new UnauthorizedActionException(
                    "No tenés permisos para eliminar tipos de ticket de este evento");
        }

        var ticketType = ticketTypeRepository.findByIdAndEventId(ticketTypeId, eventId)
                .orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));

        ticketTypeRepository.delete(ticketType);
    }
}
