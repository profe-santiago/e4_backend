package com.tickets.ticket_service.ticket.infrastructure.persistence;

import com.tickets.ticket_service.ticket.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataTicketRepository extends JpaRepository<TicketJpaEntity, UUID> {

    @Query("SELECT t FROM TicketJpaEntity t " +
            "LEFT JOIN FETCH t.orderItem oi " +
            "LEFT JOIN FETCH oi.order " +
            "WHERE t.id = :id")
    Optional<TicketJpaEntity> findByIdWithOrder(@Param("id") UUID id);

    @Query("SELECT t FROM TicketJpaEntity t " +
            "LEFT JOIN FETCH t.orderItem oi " +
            "LEFT JOIN FETCH oi.order " +
            "WHERE t.userId = :userId AND t.status = :status")
    List<TicketJpaEntity> findAllByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") TicketStatus status);
}
