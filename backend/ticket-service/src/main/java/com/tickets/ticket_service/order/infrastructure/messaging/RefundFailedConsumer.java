package com.tickets.ticket_service.order.infrastructure.messaging;

import com.tickets.ticket_service.exception.OrderNotFoundException;
import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.order.domain.OrderStatus;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.RefundFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor del evento RefundFailedEvent publicado por payment-service.
 *
 * Revierte la orden de REFUND_PENDING a CONFIRMED para que el usuario
 * pueda reintentar el reembolso.
 */
@Component
public class RefundFailedConsumer {

    private static final Logger log = LoggerFactory.getLogger(RefundFailedConsumer.class);

    private final OrderRepository orderRepository;

    public RefundFailedConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.refund-failed}")
    public void handle(RefundFailedEvent event) {
        log.warn("[CONSUME] refund.failed → orderId={} reason={}", event.getOrderId(), event.getReason());

        Order order = orderRepository.findByIdWithItems(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(event.getOrderId()));

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.warn("[CONSUME] Orden ya en CONFIRMED, ignorando mensaje duplicado → orderId={}", event.getOrderId());
            return;
        }

        order.failRefund();
        orderRepository.save(order);

        log.warn("[CONSUME] Orden revertida a CONFIRMED tras fallo de reembolso → orderId={}", order.getId());
    }
}
