package com.tickets.event_service.category;

import com.tickets.event_service.category.dto.CategoryRequest;
import com.tickets.event_service.category.dto.CategoryResponse;
import com.tickets.event_service.exception.CategoryNotFoundException;
import com.tickets.event_service.exception.DuplicateNameException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateNameException("categoría", request.getName());
        }
        Category saved = categoryRepository.save(CategoryMapper.toEntity(request));
        return CategoryMapper.toResponse(saved);
    }

    @Override
    public CategoryResponse findById(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryMapper::toResponse)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        boolean nameChanged = !category.getName().equalsIgnoreCase(request.getName());
        if (nameChanged && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateNameException("categoría", request.getName());
        }

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException(id);
        }
        categoryRepository.deleteById(id);
    }
}
