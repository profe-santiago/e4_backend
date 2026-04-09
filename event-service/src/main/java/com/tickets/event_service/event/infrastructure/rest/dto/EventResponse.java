package com.tickets.event_service.event.infrastructure.rest.dto;

import com.tickets.event_service.category.infrastructure.rest.dto.CategoryResponse;
import com.tickets.event_service.event.domain.EventStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EventResponse {

    private UUID id;
    private UUID organizerId;
    private String title;
    private String description;
    private CategoryResponse category;
    private String venue;
    private String city;
    private String country;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String imageUrl;
    private EventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
