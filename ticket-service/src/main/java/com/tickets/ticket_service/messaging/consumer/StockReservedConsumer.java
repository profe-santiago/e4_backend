package com.tickets.ticket_service.messaging.consumer;

import com.tickets.ticket_service.messaging.event.StockReservedEvent;
import com.tickets.ticket_service.order.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Escucha confirmaciones de stock desde event-service.
 * Cuando llega → confirma la orden y genera los tickets.
 */
@Component
public class StockReservedConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockReservedConsumer.class);

    private final OrderService orderService;

    public StockReservedConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.stock-reserved}")
    public void handle(StockReservedEvent event) {
        log.info("[CONSUME] stock.reserved → orderId={}", event.getOrderId());
        orderService.confirmOrder(event);
    }
}
