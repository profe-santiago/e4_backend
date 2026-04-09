package com.tickets.event_service.event.application;

import com.tickets.event_service.event.domain.Event;
import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.shared.UseCase;

import java.util.List;
import java.util.UUID;

@UseCase
public class ListMyEventsUseCase {

    private final EventRepository eventRepository;

    public ListMyEventsUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> execute(UUID organizerId) {
        return eventRepository.findAllByOrganizerId(organizerId);
    }
}
