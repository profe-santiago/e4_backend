package com.tickets.event_service.tickettype.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto secundario (salida) del bounded context TicketType.
 * Interface en el dominio — implementada en infrastructure/persistence.
 */
public interface TicketTypeRepository {

    Optional<TicketType> findById(Long id);

    Optional<TicketType> findByIdAndEventId(Long id, UUID eventId);

    /**
     * Busca con PESSIMISTIC_WRITE lock para la reserva de stock.
     * El nombre expresa la INTENCIÓN de negocio, no el mecanismo JPA.
     */
    Optional<TicketType> findByIdLocked(Long id);

    List<TicketType> findAllByEventId(UUID eventId);

    TicketType save(TicketType ticketType);

    void delete(TicketType ticketType);
}
