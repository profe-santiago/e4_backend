package com.tickets.event_service.tickettype.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataTicketTypeRepository extends JpaRepository<TicketTypeJpaEntity, Long> {

    List<TicketTypeJpaEntity> findAllByEventId(UUID eventId);

    Optional<TicketTypeJpaEntity> findByIdAndEventId(Long id, UUID eventId);

    /**
     * Busca con PESSIMISTIC_WRITE lock para evitar race conditions en la reserva de stock.
     * Garantiza que solo una transacción a la vez pueda modificar el available_quantity.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketTypeJpaEntity t WHERE t.id = :id")
    Optional<TicketTypeJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
