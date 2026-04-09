package com.tickets.ticket_service.order.application;

import com.tickets.ticket_service.exception.OrderNotFoundException;
import com.tickets.ticket_service.exception.UnauthorizedActionException;
import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderEventPublisher;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.shared.UseCase;

import java.util.UUID;

/**
 * Caso de uso: cancelación manual de orden iniciada por el usuario.
 */
@UseCase
public class CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public CancelOrderUseCase(OrderRepository orderRepository,
                               OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public Order execute(UUID orderId, UUID requesterId, boolean isAdmin) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!isAdmin && !order.getUserId().equals(requesterId)) {
            throw new UnauthorizedActionException("No tenés permisos para cancelar esta orden");
        }

        order.cancel();
        Order saved = orderRepository.save(order);

        eventPublisher.publishOrderCancelled(
                saved.getId(), saved.getUserId(), "Cancelado por el usuario");

        return saved;
    }
}
