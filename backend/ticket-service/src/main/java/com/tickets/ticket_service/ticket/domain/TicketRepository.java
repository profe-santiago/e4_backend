package com.tickets.ticket_service.ticket.domain;

import com.tickets.ticket_service.shared.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto secundario (salida) del bounded context Ticket.
 * Interface pura en el dominio — sin Spring, sin JPA.
 */
public interface TicketRepository {

    Optional<Ticket> findById(UUID id);

    /**
     * Carga el ticket con la info de la orden para poblar orderId en el dominio.
     */
    Optional<Ticket> findByIdWithOrder(UUID id);

    /**
     * Carga todos los tickets del usuario con info de orden.
     */
    PageResult<Ticket> findAllByUserIdWithOrder(UUID userId, int page, int size);

    List<Ticket> findAllByOrderId(UUID orderId);

    Optional<Ticket> findByQrCode(String qrCode);

    Ticket save(Ticket ticket);
}
