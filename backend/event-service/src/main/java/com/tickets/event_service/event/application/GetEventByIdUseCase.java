package com.tickets.event_service.event.application;

import com.tickets.event_service.event.domain.Event;
import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.shared.UseCase;

import java.util.UUID;

@UseCase
public class GetEventByIdUseCase {

    private final EventRepository eventRepository;

    public GetEventByIdUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event execute(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }
}
