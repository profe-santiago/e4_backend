package com.tickets.event_service.event.infrastructure.persistence;

import com.tickets.event_service.category.domain.Category;
import com.tickets.event_service.category.infrastructure.persistence.CategoryJpaEntity;
import com.tickets.event_service.event.domain.Event;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

/**
 * Mapper entre Event (dominio) y EventJpaEntity (infraestructura).
 * Bean de Spring — inyectable y testeable.
 *
 * Usa EntityManager.getReference() para resolver la FK de Category sin
 * cargar la entidad completa — evita el N+1 en escrituras.
 */
@Component
public class EventPersistenceMapper {

    @PersistenceContext
    private EntityManager entityManager;

    public Event toDomain(EventJpaEntity entity) {
        Event event = new Event();
        event.setId(entity.getId());
        event.setOrganizerId(entity.getOrganizerId());
        event.setTitle(entity.getTitle());
        event.setDescription(entity.getDescription());
        event.setCategory(toCategoryDomain(entity.getCategory()));
        event.setVenue(entity.getVenue());
        event.setCity(entity.getCity());
        event.setCountry(entity.getCountry());
        event.setStartDate(entity.getStartDate());
        event.setEndDate(entity.getEndDate());
        event.setImageUrl(entity.getImageUrl());
        event.setStatus(entity.getStatus());
        event.setCreatedAt(entity.getCreatedAt());
        event.setUpdatedAt(entity.getUpdatedAt());
        return event;
    }

    public EventJpaEntity toJpaEntity(Event domain) {
        EventJpaEntity entity = new EventJpaEntity();
        entity.setId(domain.getId());
        entity.setOrganizerId(domain.getOrganizerId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setCategory(toCategoryReference(domain.getCategory()));
        entity.setVenue(domain.getVenue());
        entity.setCity(domain.getCity());
        entity.setCountry(domain.getCountry());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setImageUrl(domain.getImageUrl());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private Category toCategoryDomain(CategoryJpaEntity entity) {
        if (entity == null) return null;
        return new Category(entity.getId(), entity.getName(), entity.getDescription());
    }

    /**
     * Resuelve la referencia de Category para JPA sin cargar la entidad.
     * getReference() devuelve un proxy — suficiente para establecer el FK.
     */
    private CategoryJpaEntity toCategoryReference(Category domain) {
        if (domain == null) return null;
        return entityManager.getReference(CategoryJpaEntity.class, domain.getId());
    }
}
