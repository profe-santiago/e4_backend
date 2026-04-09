package com.tickets.event_service.category.domain;

import java.util.List;
import java.util.Optional;

/**
 * Puerto secundario (salida) del aggregate Category.
 * Interface en el dominio — implementada en infrastructure/persistence.
 */
public interface CategoryRepository {

    Optional<Category> findById(Long id);

    List<Category> findAll();

    boolean existsByNameIgnoreCase(String name);

    boolean existsById(Long id);

    Category save(Category category);

    void deleteById(Long id);
}
