package com.tickets.event_service.tickettype.infrastructure.messaging;

import com.tickets.event_service.tickettype.application.ReserveStockUseCase;
import com.tickets.event_service.tickettype.infrastructure.messaging.dto.StockReserveCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Consumer delegado — adaptador de mensajería de entrada.
 * Solo recibe el mensaje, transforma al Command del UseCase y delega.
 * Sin lógica de negocio — sin transacciones aquí.
 */
@Component
public class StockReserveConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockReserveConsumer.class);

    private final ReserveStockUseCase reserveStockUseCase;

    public StockReserveConsumer(ReserveStockUseCase reserveStockUseCase) {
        this.reserveStockUseCase = reserveStockUseCase;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.stock-reserve}")
    public void handle(StockReserveCommand command) {
        log.info("[CONSUME] stock.reserve → orderId={}", command.getOrderId());

        List<ReserveStockUseCase.ReservationItem> items = command.getItems().stream()
                .map(item -> new ReserveStockUseCase.ReservationItem(
                        item.getEventId(),
                        item.getTicketTypeId(),
                        item.getQuantity()
                ))
                .toList();

        reserveStockUseCase.execute(command.getOrderId(), items);
    }
}
