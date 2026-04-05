package com.tickets.event_service.messaging.consumer;

import com.tickets.event_service.messaging.StockReservationService;
import com.tickets.event_service.messaging.event.StockReserveCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer delgado — solo recibe el mensaje y delega en StockReservationService.
 * La lógica de negocio y la transacción viven en el service, no en el listener.
 */
@Component
public class StockReserveConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockReserveConsumer.class);

    private final StockReservationService stockReservationService;

    public StockReserveConsumer(StockReservationService stockReservationService) {
        this.stockReservationService = stockReservationService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.stock-reserve}")
    public void handle(StockReserveCommand command) {
        log.info("[CONSUME] stock.reserve → orderId={}", command.getOrderId());
        stockReservationService.reserve(command);
    }
}
