package com.tickets.ticket_service.order.infrastructure.messaging;

import com.tickets.ticket_service.exception.OrderNotFoundException;
import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderEventPublisher;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.RefundCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Consumidor del evento RefundCompletedEvent publicado por payment-service.
 *
 * Transiciona la orden de CONFIRMED a REFUNDED y publica OrderRefundedEvent
 * para que event-service pueda liberar el stock.
 */
@Component
public class RefundCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(RefundCompletedConsumer.class);

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public RefundCompletedConsumer(OrderRepository orderRepository,
                                   OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.refund-completed}")
    public void handle(RefundCompletedEvent event) {
        log.info("[CONSUME] refund.completed → orderId={}", event.getOrderId());

        Order order = orderRepository.findByIdWithItems(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(event.getOrderId()));

        order.refund();
        Order saved = orderRepository.save(order);

        List<OrderEventPublisher.StockReleaseItem> stockItems = saved.getItems().stream()
                .map(i -> new OrderEventPublisher.StockReleaseItem(
                        i.getEventId(), i.getTicketTypeId(), i.getQuantity()))
                .toList();

        eventPublisher.publishOrderRefunded(saved.getId(), saved.getUserId(), stockItems);
        log.info("[CONSUME] Orden reembolsada → orderId={}", saved.getId());
    }
}
