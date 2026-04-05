package com.tickets.ticket_service.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @Query("SELECT t FROM Ticket t JOIN FETCH t.orderItem oi JOIN FETCH oi.order WHERE t.userId = :userId")
    List<Ticket> findAllByUserIdWithOrder(@Param("userId") UUID userId);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.orderItem oi JOIN FETCH oi.order WHERE t.id = :id")
    Optional<Ticket> findByIdWithOrder(@Param("id") UUID id);

    Optional<Ticket> findByQrCode(String qrCode);
}
