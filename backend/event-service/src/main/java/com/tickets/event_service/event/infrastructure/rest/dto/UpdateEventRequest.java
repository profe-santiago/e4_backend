package com.tickets.event_service.event.infrastructure.rest.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UpdateEventRequest {

    @Size(max = 255)
    private String title;

    private String description;

    private Long categoryId;

    @Size(max = 255)
    private String venue;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String country;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Size(max = 500)
    private String imageUrl;
}
