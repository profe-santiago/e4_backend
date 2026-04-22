package com.tickets.event_service.category.application;

import com.tickets.event_service.category.domain.Category;
import com.tickets.event_service.category.domain.CategoryRepository;
import com.tickets.event_service.shared.UseCase;

import java.util.List;

@UseCase
public class ListCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public ListCategoriesUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> execute() {
        return categoryRepository.findAll();
    }
}
