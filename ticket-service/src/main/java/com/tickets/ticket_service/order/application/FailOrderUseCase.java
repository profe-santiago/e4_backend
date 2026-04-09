package com.tickets.ticket_service.order.application;

import com.tickets.ticket_service.exception.OrderNotFoundException;
import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderEventPublisher;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.shared.UseCase;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: marcar la orden como FAILED.
 * Disparado por: sin stock (StockFailedConsumer) o pago rechazado (PaymentFailedConsumer).
 */
@UseCase
public class FailOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public FailOrderUseCase(OrderRepository orderRepository,
                             OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(UUID orderId, String reason) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.fail();
        orderRepository.save(order);

        eventPublisher.publishOrderCancelled(order.getId(), order.getUserId(), reason);
    }
}
