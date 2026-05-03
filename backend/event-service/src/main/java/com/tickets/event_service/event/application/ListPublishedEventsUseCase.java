package com.tickets.event_service.event.application;

import com.tickets.event_service.event.domain.Event;
import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.event.domain.EventStatus;
import com.tickets.event_service.shared.PageResult;
import com.tickets.event_service.shared.UseCase;

@UseCase
public class ListPublishedEventsUseCase {

    private final EventRepository eventRepository;

    public ListPublishedEventsUseCase(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public PageResult<Event> execute(Long categoryId, String search, String city, String venue, int page, int size) {
        return eventRepository.findPublished(EventStatus.PUBLISHED, categoryId, search, city, venue, page, size);
    }
}
