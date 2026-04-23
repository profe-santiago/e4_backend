package com.tickets.event_service.event.infrastructure.rest;

import com.tickets.event_service.category.infrastructure.rest.CategoryRestMapper;
import com.tickets.event_service.event.application.dto.CreateEventCommand;
import com.tickets.event_service.event.application.dto.UpdateEventCommand;
import com.tickets.event_service.event.domain.Event;
import com.tickets.event_service.event.infrastructure.rest.dto.CreateEventRequest;
import com.tickets.event_service.event.infrastructure.rest.dto.EventResponse;
import com.tickets.event_service.event.infrastructure.rest.dto.UpdateEventRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper REST para Event: HTTP request ↔ Command + Domain → HTTP response.
 * Bean de Spring — inyectable y testeable.
 * Delega el mapeo de Category al CategoryRestMapper — single responsibility.
 */
@Component
public class EventRestMapper {

    private final CategoryRestMapper categoryRestMapper;

    public EventRestMapper(CategoryRestMapper categoryRestMapper) {
        this.categoryRestMapper = categoryRestMapper;
    }

    public CreateEventCommand toCreateCommand(CreateEventRequest request, UUID organizerId) {
        return new CreateEventCommand(
                organizerId,
                request.getTitle(),
                request.getDescription(),
                request.getCategoryId(),
                request.getVenue(),
                request.getCity(),
                request.getCountry(),
                request.getStartDate(),
                request.getEndDate(),
                request.getImageUrl()
        );
    }

    public UpdateEventCommand toUpdateCommand(UpdateEventRequest request, UUID requesterId, boolean isAdmin) {
        return new UpdateEventCommand(
                requesterId,
                isAdmin,
                request.getTitle(),
                request.getDescription(),
                request.getCategoryId(),
                request.getVenue(),
                request.getCity(),
                request.getCountry(),
                request.getStartDate(),
                request.getEndDate(),
                request.getImageUrl()
        );
    }

    public EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .organizerId(event.getOrganizerId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory() != null
                        ? categoryRestMapper.toResponse(event.getCategory())
                        : null)
                .venue(event.getVenue())
                .city(event.getCity())
                .country(event.getCountry())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .imageUrl(event.getImageUrl())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
