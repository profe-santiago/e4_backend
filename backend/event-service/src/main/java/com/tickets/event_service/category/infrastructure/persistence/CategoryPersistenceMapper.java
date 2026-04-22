package com.tickets.event_service.category.infrastructure.persistence;

import com.tickets.event_service.category.domain.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper entre el objeto de dominio Category y la entidad JPA CategoryJpaEntity.
 * Bean de Spring — no mapper estático.
 */
@Component
class CategoryPersistenceMapper {

    Category toDomain(CategoryJpaEntity entity) {
        return new Category(entity.getId(), entity.getName(), entity.getDescription());
    }

    CategoryJpaEntity toJpaEntity(Category domain) {
        CategoryJpaEntity entity = new CategoryJpaEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        return entity;
    }
}
