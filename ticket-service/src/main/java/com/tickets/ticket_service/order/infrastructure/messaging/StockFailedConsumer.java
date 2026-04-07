package com.tickets.ticket_service.order.infrastructure.messaging;

import com.tickets.ticket_service.order.application.FailOrderUseCase;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.StockFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador primario: escucha rechazos de stock desde event-service.
 * Cuando llega → marca la orden como FAILED.
 */
@Component
public class StockFailedConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockFailedConsumer.class);

    private final FailOrderUseCase failOrder;

    public StockFailedConsumer(FailOrderUseCase failOrder) {
        this.failOrder = failOrder;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.stock-failed}")
    public void handle(StockFailedEvent event) {
        log.info("[CONSUME] stock.failed → orderId={}, reason={}", event.getOrderId(), event.getReason());
        failOrder.execute(event.getOrderId(), event.getReason());
    }
}
