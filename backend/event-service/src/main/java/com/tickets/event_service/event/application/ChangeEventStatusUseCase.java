package com.tickets.event_service.event.application;

import com.tickets.event_service.event.domain.Event;
import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.event.domain.EventStatus;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.exception.UnauthorizedActionException;
import com.tickets.event_service.shared.UseCase;

import java.util.UUID;

@UseCase
public class ChangeEventStatusUseCase {

    private final EventRepository eventRepository;

    public ChangeEventStatusUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event execute(UUID eventId, EventStatus newStatus, UUID requesterId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!isAdmin && !event.getOrganizerId().equals(requesterId)) {
            throw new UnauthorizedActionException("No tenés permisos para cambiar el estado de este evento");
        }

        // La validación de la transición vive en el dominio (Event.changeStatus)
        event.changeStatus(newStatus);
        return eventRepository.save(event);
    }
}
