package com.tickets.event_service.event;

import com.tickets.event_service.event.dto.CreateEventRequest;
import com.tickets.event_service.event.dto.EventResponse;
import com.tickets.event_service.event.dto.UpdateEventRequest;
import com.tickets.event_service.shared.PaginatedResponse;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface EventService {

    EventResponse create(CreateEventRequest request, Authentication auth);

    EventResponse findById(UUID id);

    PaginatedResponse<EventResponse> findPublished(Long categoryId, int page, int size);

    List<EventResponse> findMyEvents(Authentication auth);

    EventResponse update(UUID id, UpdateEventRequest request, Authentication auth);

    EventResponse changeStatus(UUID id, EventStatus newStatus, Authentication auth);

    void delete(UUID id, Authentication auth);
}
