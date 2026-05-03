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

    @Query(value = "SELECT e FROM EventJpaEntity e LEFT JOIN FETCH e.category " +
                   "WHERE e.status = :status " +
                   "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
                   "AND (:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                   "AND (:city IS NULL OR LOWER(e.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
                   "AND (:venue IS NULL OR LOWER(e.venue) LIKE LOWER(CONCAT('%', :venue, '%')))",
           countQuery = "SELECT COUNT(e) FROM EventJpaEntity e " +
                        "WHERE e.status = :status " +
                        "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
                        "AND (:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:city IS NULL OR LOWER(e.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
                        "AND (:venue IS NULL OR LOWER(e.venue) LIKE LOWER(CONCAT('%', :venue, '%')))")
    Page<EventJpaEntity> findAllByFilters(
            @Param("status") EventStatus status,
            @Param("categoryId") Long categoryId,
            @Param("search") String search,
            @Param("city") String city,
            @Param("venue") String venue,
            Pageable pageable);

    List<EventJpaEntity> findAllByOrganizerId(UUID organizerId);
}
