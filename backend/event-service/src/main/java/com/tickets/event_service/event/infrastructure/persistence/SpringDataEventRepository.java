package com.tickets.event_service.event.infrastructure.persistence;

import com.tickets.event_service.event.domain.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA — detalle de implementación, package-private.
 * El exterior solo ve el puerto EventRepository del dominio.
 */
interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, UUID> {

    @Query(value = "SELECT e FROM EventJpaEntity e LEFT JOIN FETCH e.category WHERE e.status = :status",
           countQuery = "SELECT COUNT(e) FROM EventJpaEntity e WHERE e.status = :status")
    Page<EventJpaEntity> findAllByStatusWithCategory(
            @Param("status") EventStatus status, Pageable pageable);

    @Query(value = "SELECT e FROM EventJpaEntity e LEFT JOIN FETCH e.category WHERE e.status = :status AND e.category.id = :categoryId",
           countQuery = "SELECT COUNT(e) FROM EventJpaEntity e WHERE e.status = :status AND e.category.id = :categoryId")
    Page<EventJpaEntity> findAllByStatusAndCategoryIdWithCategory(
            @Param("status") EventStatus status,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    List<EventJpaEntity> findAllByOrganizerId(UUID organizerId);
}
