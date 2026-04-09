package com.tickets.ticket_service.order.domain;

import com.tickets.ticket_service.shared.PageResult;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto secundario (salida) del aggregate Order.
 * Interface pura en el dominio — sin Spring, sin JPA.
 */
public interface OrderRepository {

    Optional<Order> findById(UUID id);

    /**
     * Carga la orden con sus ítems — evita N+1.
     */
    Optional<Order> findByIdWithItems(UUID id);

    PageResult<Order> findByUserId(UUID userId, int page, int size);

    Order save(Order order);
}
