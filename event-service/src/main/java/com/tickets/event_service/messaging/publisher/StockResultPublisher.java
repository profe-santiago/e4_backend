package com.tickets.event_service.messaging.publisher;

import com.tickets.event_service.messaging.event.StockFailedEvent;
import com.tickets.event_service.messaging.event.StockReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StockResultPublisher {

    private static final Logger log = LoggerFactory.getLogger(StockResultPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-keys.stock-reserved}")
    private String rkStockReserved;

    @Value("${app.rabbitmq.routing-keys.stock-failed}")
    private String rkStockFailed;

    public StockResultPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishReserved(StockReservedEvent event) {
        log.info("[PUBLISH] stock.reserved → orderId={}", event.getOrderId());
        rabbitTemplate.convertAndSend(exchange, rkStockReserved, event);
    }

    public void publishFailed(StockFailedEvent event) {
        log.info("[PUBLISH] stock.failed → orderId={}, reason={}", event.getOrderId(), event.getReason());
        rabbitTemplate.convertAndSend(exchange, rkStockFailed, event);
    }
}
