package com.tickets.event_service.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    /**
     * JOIN FETCH para evitar N+1 al cargar la categoría en listados públicos.
     * Se separa el countQuery por limitación de Spring Data con JOIN FETCH + Page.
     */
    @Query(value = "SELECT e FROM Event e LEFT JOIN FETCH e.category WHERE e.status = :status",
           countQuery = "SELECT COUNT(e) FROM Event e WHERE e.status = :status")
    Page<Event> findAllByStatusWithCategory(@Param("status") EventStatus status, Pageable pageable);

    @Query(value = "SELECT e FROM Event e LEFT JOIN FETCH e.category WHERE e.status = :status AND e.category.id = :categoryId",
           countQuery = "SELECT COUNT(e) FROM Event e WHERE e.status = :status AND e.category.id = :categoryId")
    Page<Event> findAllByStatusAndCategoryIdWithCategory(
            @Param("status") EventStatus status,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    List<Event> findAllByOrganizerId(UUID organizerId);
}
