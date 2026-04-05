package com.tickets.ticket_service.messaging.publisher;

import com.tickets.ticket_service.messaging.event.OrderCancelledEvent;
import com.tickets.ticket_service.messaging.event.OrderConfirmedEvent;
import com.tickets.ticket_service.messaging.event.StockReserveCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitOrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-keys.stock-reserve}")
    private String rkStockReserve;

    @Value("${app.rabbitmq.routing-keys.order-confirmed}")
    private String rkOrderConfirmed;

    @Value("${app.rabbitmq.routing-keys.order-cancelled}")
    private String rkOrderCancelled;

    public RabbitOrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishStockReserve(StockReserveCommand command) {
        log.info("[PUBLISH] stock.reserve → orderId={}", command.getOrderId());
        rabbitTemplate.convertAndSend(exchange, rkStockReserve, command);
    }

    @Override
    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        log.info("[PUBLISH] order.confirmed → orderId={}", event.getOrderId());
        rabbitTemplate.convertAndSend(exchange, rkOrderConfirmed, event);
    }

    @Override
    public void publishOrderCancelled(OrderCancelledEvent event) {
        log.info("[PUBLISH] order.cancelled → orderId={}", event.getOrderId());
        rabbitTemplate.convertAndSend(exchange, rkOrderCancelled, event);
    }
}
