package com.tickets.event_service.event.domain;

import com.tickets.event_service.shared.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto secundario (salida) del aggregate Event.
 * Interface pura en el dominio — sin Spring, sin JPA.
 * La implementación vive en infrastructure/persistence.
 */
public interface EventRepository {

    Optional<Event> findById(UUID id);

    boolean existsById(UUID id);

    /**
     * Listado paginado de eventos publicados, con filtro opcional por categoría.
     * Devuelve PageResult<Event> — abstracción de dominio, no Spring Page.
     */
    PageResult<Event> findPublished(EventStatus status, Long categoryId, String search, String city, String venue, int page, int size);

    List<Event> findAllByOrganizerId(UUID organizerId);

    Event save(Event event);

    void delete(Event event);
}
