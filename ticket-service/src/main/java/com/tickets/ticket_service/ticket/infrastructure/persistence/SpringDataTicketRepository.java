package com.tickets.ticket_service.ticket.infrastructure.persistence;

import com.tickets.ticket_service.ticket.domain.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = "SELECT t FROM TicketJpaEntity t " +
            "LEFT JOIN FETCH t.orderItem oi " +
            "LEFT JOIN FETCH oi.order " +
            "WHERE t.userId = :userId",
           countQuery = "SELECT COUNT(t) FROM TicketJpaEntity t WHERE t.userId = :userId")
    Page<TicketJpaEntity> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT t FROM TicketJpaEntity t " +
            "LEFT JOIN FETCH t.orderItem oi " +
            "LEFT JOIN FETCH oi.order o " +
            "WHERE o.id = :orderId")
    List<TicketJpaEntity> findAllByOrderId(@Param("orderId") UUID orderId);

    Optional<TicketJpaEntity> findByQrCode(String qrCode);
}
