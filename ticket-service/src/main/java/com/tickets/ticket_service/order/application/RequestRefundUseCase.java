package com.tickets.ticket_service.order.application;

import com.tickets.ticket_service.exception.InvalidOrderStateException;
import com.tickets.ticket_service.exception.OrderNotFoundException;
import com.tickets.ticket_service.exception.UnauthorizedActionException;
import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderEventPublisher;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.order.domain.OrderStatus;
import com.tickets.ticket_service.shared.UseCase;

import java.util.UUID;

/**
 * Caso de uso: iniciar el flujo de reembolso de una orden CONFIRMED.
 *
 * La orden permanece en CONFIRMED hasta que payment-service confirme el reembolso
 * en Stripe. La transición a REFUNDED ocurre en RefundCompletedConsumer.
 *
 * Responde 202 ACCEPTED ya que el proceso es asíncrono.
 */
@UseCase
public class RequestRefundUseCase {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public RequestRefundUseCase(OrderRepository orderRepository,
                                OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public Order execute(UUID orderId, UUID requesterId, boolean isAdmin) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!isAdmin && !order.getUserId().equals(requesterId)) {
            throw new UnauthorizedActionException("No tenés permisos para solicitar reembolso de esta orden");
        }

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException(order.getStatus(), OrderStatus.REFUNDED);
        }

        eventPublisher.publishRefundInitiated(order.getId(), order.getUserId());

        return order;
    }
}
