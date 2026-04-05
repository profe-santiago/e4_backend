package com.tickets.event_service.category;

import com.tickets.event_service.category.dto.CategoryRequest;
import com.tickets.event_service.category.dto.CategoryResponse;

/**
 * Mapper estático para Category.
 * Centraliza la conversión entity ↔ DTO para evitar lógica de mapeo dispersa.
 */
public final class CategoryMapper {

    private CategoryMapper() {}

    public static CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    public static Category toEntity(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        return category;
    }
}
