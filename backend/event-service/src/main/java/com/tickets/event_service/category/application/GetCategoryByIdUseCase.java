package com.tickets.event_service.category.application;

import com.tickets.event_service.category.domain.Category;
import com.tickets.event_service.category.domain.CategoryRepository;
import com.tickets.event_service.exception.CategoryNotFoundException;
import com.tickets.event_service.shared.UseCase;

@UseCase
public class GetCategoryByIdUseCase {

    private final CategoryRepository categoryRepository;

    public GetCategoryByIdUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category execute(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
