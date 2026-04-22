package com.tickets.event_service.tickettype.infrastructure.messaging;

import com.tickets.event_service.tickettype.application.ReleaseStockUseCase;
import com.tickets.event_service.tickettype.infrastructure.messaging.dto.OrderCancelledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Consumidor del evento OrderCancelledEvent publicado por ticket-service.
 *
 * Libera el stock reservado cuando una orden es cancelada por el usuario
 * o marcada como FAILED (sin stock o pago rechazado).
 */
@Component
public class OrderCancelledConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelledConsumer.class);

    private final ReleaseStockUseCase releaseStockUseCase;

    public OrderCancelledConsumer(ReleaseStockUseCase releaseStockUseCase) {
        this.releaseStockUseCase = releaseStockUseCase;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.order-cancelled}")
    public void handle(OrderCancelledEvent event) {
        log.info("[CONSUME] order.cancelled → orderId={}, reason={}", event.getOrderId(), event.getReason());

        if (event.getItems() == null || event.getItems().isEmpty()) {
            log.warn("[CONSUME] OrderCancelledEvent sin items — nada que liberar: orderId={}", event.getOrderId());
            return;
        }

        List<ReleaseStockUseCase.ReleaseItem> items = event.getItems().stream()
                .map(i -> new ReleaseStockUseCase.ReleaseItem(i.getEventId(), i.getTicketTypeId(), i.getQuantity()))
                .toList();

        releaseStockUseCase.execute(event.getOrderId(), items);
    }
}
