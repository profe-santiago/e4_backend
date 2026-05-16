package com.tickets.event_service.event.application;

import com.tickets.event_service.event.domain.Event;
import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.event.domain.EventStatus;
import com.tickets.event_service.exception.EventNotDeletableException;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.exception.UnauthorizedActionException;
import com.tickets.event_service.shared.UseCase;

import java.util.UUID;

@UseCase
public class DeleteEventUseCase {

    private final EventRepository eventRepository;

    public DeleteEventUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void execute(UUID eventId, UUID requesterId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!isAdmin && !event.getOrganizerId().equals(requesterId)) {
            throw new UnauthorizedActionException("No tenés permisos para eliminar este evento");
        }

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new EventNotDeletableException(event.getStatus());
        }

        eventRepository.delete(event);
    }
}
