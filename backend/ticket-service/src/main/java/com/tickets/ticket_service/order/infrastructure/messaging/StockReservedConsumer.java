package com.tickets.ticket_service.order.infrastructure.messaging;

import com.tickets.ticket_service.order.application.ConfirmOrderUseCase;
import com.tickets.ticket_service.order.domain.StockConfirmationItem;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.StockReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador primario: escucha confirmaciones de stock desde event-service.
 * Traduce el DTO de mensajería a objetos de dominio y delega al UseCase.
 */
@Component
public class StockReservedConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockReservedConsumer.class);

    private final ConfirmOrderUseCase confirmOrder;

    public StockReservedConsumer(ConfirmOrderUseCase confirmOrder) {
        this.confirmOrder = confirmOrder;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.stock-reserved}")
    public void handle(StockReservedEvent event) {
        log.info("[CONSUME] stock.reserved → orderId={}", event.getOrderId());
        List<StockConfirmationItem> reservedItems = event.getItems().stream()
                .map(i -> new StockConfirmationItem(
                        i.getEventId(), i.getTicketTypeId(), i.getQuantity(), i.getUnitPrice()))
                .toList();
        confirmOrder.execute(event.getOrderId(), reservedItems);
    }
}
