package com.tickets.event_service.category.application;

import com.tickets.event_service.category.application.dto.CategoryCommand;
import com.tickets.event_service.category.domain.Category;
import com.tickets.event_service.category.domain.CategoryRepository;
import com.tickets.event_service.exception.DuplicateNameException;
import com.tickets.event_service.shared.UseCase;

@UseCase
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category execute(CategoryCommand command) {
        if (categoryRepository.existsByNameIgnoreCase(command.name())) {
            throw new DuplicateNameException("categoría", command.name());
        }
        Category category = new Category(null, command.name().trim(), command.description());
        return categoryRepository.save(category);
    }
}
