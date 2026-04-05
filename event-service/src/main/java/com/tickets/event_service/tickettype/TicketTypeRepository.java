package com.tickets.event_service.tickettype;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findAllByEventId(UUID eventId);

    Optional<TicketType> findByIdAndEventId(Long id, UUID eventId);

    /**
     * PESSIMISTIC_WRITE lock — bloquea la fila en DB para prevenir ventas dobles.
     * Solo usar dentro de un @Transactional, liberado al hacer commit/rollback.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketType t WHERE t.id = :id")
    Optional<TicketType> findByIdForUpdate(@Param("id") Long id);
}
