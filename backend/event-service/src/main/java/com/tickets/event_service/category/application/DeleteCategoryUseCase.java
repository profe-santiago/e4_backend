package com.tickets.event_service.category.application;

import com.tickets.event_service.category.domain.CategoryRepository;
import com.tickets.event_service.exception.CategoryNotFoundException;
import com.tickets.event_service.shared.UseCase;

@UseCase
public class DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public DeleteCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void execute(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException(id);
        }
        categoryRepository.deleteById(id);
    }
}
