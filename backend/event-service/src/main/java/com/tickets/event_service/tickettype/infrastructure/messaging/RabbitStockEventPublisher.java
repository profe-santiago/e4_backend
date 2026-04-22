package com.tickets.event_service.tickettype.infrastructure.messaging;

import com.tickets.event_service.tickettype.domain.ReservedStockItem;
import com.tickets.event_service.tickettype.domain.StockEventPublisher;
import com.tickets.event_service.tickettype.infrastructure.messaging.dto.StockFailedEvent;
import com.tickets.event_service.tickettype.infrastructure.messaging.dto.StockReservedEvent;
import com.tickets.event_service.tickettype.infrastructure.messaging.dto.StockReservedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador de mensajería — implementa el puerto StockEventPublisher del dominio.
 * Convierte los value objects del dominio en DTOs de RabbitMQ.
 */
@Component
public class RabbitStockEventPublisher implements StockEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitStockEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-keys.stock-reserved}")
    private String rkStockReserved;

    @Value("${app.rabbitmq.routing-keys.stock-failed}")
    private String rkStockFailed;

    public RabbitStockEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishReserved(UUID orderId, List<ReservedStockItem> reservedItems) {
        log.info("[PUBLISH] stock.reserved → orderId={}", orderId);

        List<StockReservedItem> dtoItems = reservedItems.stream()
                .map(item -> new StockReservedItem(
                        item.eventId(),
                        item.ticketTypeId(),
                        item.quantity(),
                        item.unitPrice().amount()
                ))
                .toList();

        rabbitTemplate.convertAndSend(exchange, rkStockReserved,
                new StockReservedEvent(orderId, dtoItems));
    }

    @Override
    public void publishFailed(UUID orderId, String reason) {
        log.info("[PUBLISH] stock.failed → orderId={}, reason={}", orderId, reason);
        rabbitTemplate.convertAndSend(exchange, rkStockFailed,
                new StockFailedEvent(orderId, reason));
    }
}
