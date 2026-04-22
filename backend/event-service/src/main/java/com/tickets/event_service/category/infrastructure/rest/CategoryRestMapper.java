package com.tickets.event_service.category.infrastructure.rest;

import com.tickets.event_service.category.application.dto.CategoryCommand;
import com.tickets.event_service.category.domain.Category;
import com.tickets.event_service.category.infrastructure.rest.dto.CategoryRequest;
import com.tickets.event_service.category.infrastructure.rest.dto.CategoryResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper REST para Category: HTTP request ↔ Command + Domain → HTTP response.
 * Bean de Spring — inyectable y testeable.
 */
@Component
public class CategoryRestMapper {

    public CategoryCommand toCommand(CategoryRequest request) {
        return new CategoryCommand(request.getName(), request.getDescription());
    }

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
