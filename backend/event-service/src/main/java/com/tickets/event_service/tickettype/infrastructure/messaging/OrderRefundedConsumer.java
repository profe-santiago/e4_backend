package com.tickets.event_service.tickettype.infrastructure.messaging;

import com.tickets.event_service.tickettype.application.ReleaseStockUseCase;
import com.tickets.event_service.tickettype.infrastructure.messaging.dto.OrderRefundedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Consumidor del evento OrderRefundedEvent publicado por ticket-service.
 *
 * Libera el stock reservado cuando una orden CONFIRMED es reembolsada,
 * permitiendo que esos tickets vuelvan a estar disponibles para otros compradores.
 */
@Component
public class OrderRefundedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderRefundedConsumer.class);

    private final ReleaseStockUseCase releaseStockUseCase;

    public OrderRefundedConsumer(ReleaseStockUseCase releaseStockUseCase) {
        this.releaseStockUseCase = releaseStockUseCase;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.order-refunded}")
    public void handle(OrderRefundedEvent event) {
        log.info("[CONSUME] order.refunded → orderId={}", event.getOrderId());

        if (event.getItems() == null || event.getItems().isEmpty()) {
            log.warn("[CONSUME] OrderRefundedEvent sin items — nada que liberar: orderId={}", event.getOrderId());
            return;
        }

        List<ReleaseStockUseCase.ReleaseItem> items = event.getItems().stream()
                .map(i -> new ReleaseStockUseCase.ReleaseItem(i.getEventId(), i.getTicketTypeId(), i.getQuantity()))
                .toList();

        releaseStockUseCase.execute(event.getOrderId(), items);
    }
}
