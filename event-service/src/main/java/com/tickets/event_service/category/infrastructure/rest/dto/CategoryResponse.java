package com.tickets.event_service.category.infrastructure.rest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
}
