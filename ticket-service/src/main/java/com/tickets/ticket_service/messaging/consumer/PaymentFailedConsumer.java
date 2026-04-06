package com.tickets.ticket_service.messaging.consumer;

import com.tickets.ticket_service.messaging.event.PaymentFailedEvent;
import com.tickets.ticket_service.order.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Escucha rechazos de pago desde payment-service.
 * Cuando llega → marca la orden como FAILED (libera el stock en event-service via OrderCancelledEvent).
 */
@Component
public class PaymentFailedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentFailedConsumer.class);

    private final OrderService orderService;

    public PaymentFailedConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.payment-failed}")
    public void handle(PaymentFailedEvent event) {
        log.info("[CONSUME] payment.failed → orderId={}, reason={}", event.getOrderId(), event.getReason());
        orderService.failOrder(event.getOrderId(), "Pago rechazado: " + event.getReason());
    }
}
