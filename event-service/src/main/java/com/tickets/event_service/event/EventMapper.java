package com.tickets.event_service.event;

import com.tickets.event_service.category.CategoryMapper;
import com.tickets.event_service.event.dto.EventResponse;

/**
 * Mapper estático para Event.
 * Delega el mapeo de Category a CategoryMapper — cada mapper es responsable de su dominio.
 */
public final class EventMapper {

    private EventMapper() {}

    public static EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .organizerId(event.getOrganizerId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory() != null
                        ? CategoryMapper.toResponse(event.getCategory())
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
