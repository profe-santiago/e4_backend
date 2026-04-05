package com.tickets.ticket_service.messaging.consumer;

import com.tickets.ticket_service.messaging.event.StockFailedEvent;
import com.tickets.ticket_service.order.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Escucha rechazos de stock desde event-service.
 * Cuando llega → marca la orden como FAILED.
 */
@Component
public class StockFailedConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockFailedConsumer.class);

    private final OrderService orderService;

    public StockFailedConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.stock-failed}")
    public void handle(StockFailedEvent event) {
        log.info("[CONSUME] stock.failed → orderId={}, reason={}", event.getOrderId(), event.getReason());
        orderService.failOrder(event.getOrderId(), event.getReason());
    }
}
