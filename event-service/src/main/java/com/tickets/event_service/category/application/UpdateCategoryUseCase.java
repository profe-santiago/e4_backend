package com.tickets.event_service.category.application;

import com.tickets.event_service.category.application.dto.CategoryCommand;
import com.tickets.event_service.category.domain.Category;
import com.tickets.event_service.category.domain.CategoryRepository;
import com.tickets.event_service.exception.CategoryNotFoundException;
import com.tickets.event_service.exception.DuplicateNameException;
import com.tickets.event_service.shared.UseCase;

@UseCase
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public UpdateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category execute(Long id, CategoryCommand command) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        boolean nameChanged = !category.getName().equalsIgnoreCase(command.name());
        if (nameChanged && categoryRepository.existsByNameIgnoreCase(command.name())) {
            throw new DuplicateNameException("categoría", command.name());
        }

        category.rename(command.name());
        category.updateDescription(command.description());
        return categoryRepository.save(category);
    }
}
