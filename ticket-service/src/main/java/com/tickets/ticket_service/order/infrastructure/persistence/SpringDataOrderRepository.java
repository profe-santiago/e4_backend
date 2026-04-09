package com.tickets.ticket_service.order.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {

    Page<OrderJpaEntity> findAllByUserId(UUID userId, Pageable pageable);

    @Query("SELECT o FROM OrderJpaEntity o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderJpaEntity> findByIdWithItems(@Param("id") UUID id);
}
