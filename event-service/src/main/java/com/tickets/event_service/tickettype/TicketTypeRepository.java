package com.tickets.event_service.tickettype;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findAllByEventId(UUID eventId);

    Optional<TicketType> findByIdAndEventId(Long id, UUID eventId);
}
