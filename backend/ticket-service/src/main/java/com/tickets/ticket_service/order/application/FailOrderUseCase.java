package com.tickets.ticket_service.order.application;

import com.tickets.ticket_service.exception.OrderNotFoundException;
import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderEventPublisher;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.order.domain.OrderStatus;
import com.tickets.ticket_service.shared.UseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;


/**
 * Caso de uso: marcar la orden como FAILED.
 * Disparado por: sin stock (StockFailedConsumer) o pago rechazado (PaymentFailedConsumer).
 */
@UseCase
public class FailOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(FailOrderUseCase.class);

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

        if (order.getStatus() == OrderStatus.FAILED || order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("[UC] FailOrder — orden ya en estado terminal {}, ignorando (idempotencia): orderId={}", order.getStatus(), orderId);
            return;
        }

        order.fail();
        Order saved = orderRepository.save(order);

        List<OrderEventPublisher.StockReleaseItem> stockItems = saved.getItems().stream()
                .map(i -> new OrderEventPublisher.StockReleaseItem(i.getEventId(), i.getTicketTypeId(), i.getQuantity()))
                .toList();

        eventPublisher.publishOrderCancelled(saved.getId(), saved.getUserId(), reason, stockItems);
    }
}
