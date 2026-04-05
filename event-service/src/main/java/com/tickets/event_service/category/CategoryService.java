package com.tickets.event_service.category;

import com.tickets.event_service.category.dto.CategoryRequest;
import com.tickets.event_service.category.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    CategoryResponse findById(Long id);

    List<CategoryResponse> findAll();

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}
