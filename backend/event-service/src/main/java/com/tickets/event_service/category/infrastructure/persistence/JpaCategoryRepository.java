package com.tickets.event_service.category.infrastructure.persistence;

import com.tickets.event_service.category.domain.Category;
import com.tickets.event_service.category.domain.CategoryRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia — implementa el puerto CategoryRepository del dominio
 * usando Spring Data JPA internamente.
 */
@Repository
@Transactional(readOnly = true)
public class JpaCategoryRepository implements CategoryRepository {

    private final SpringDataCategoryRepository springData;
    private final CategoryPersistenceMapper mapper;

    public JpaCategoryRepository(SpringDataCategoryRepository springData,
                                  CategoryPersistenceMapper mapper) {
        this.springData = springData;
        this.mapper = mapper;
    }

    @Override
    public Optional<Category> findById(Long id) {
        return springData.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return springData.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return springData.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsById(Long id) {
        return springData.existsById(id);
    }

    @Override
    @Transactional
    public Category save(Category category) {
        CategoryJpaEntity entity = mapper.toJpaEntity(category);
        return mapper.toDomain(springData.save(entity));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        springData.deleteById(id);
    }
}
