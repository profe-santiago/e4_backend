package com.tickets.ticket_service.order.infrastructure.messaging;

import com.tickets.ticket_service.order.domain.OrderEventPublisher;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.OrderCancelledEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.OrderConfirmedEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.StockReserveCommand;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.StockReserveItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador secundario: implementa el puerto OrderEventPublisher usando RabbitMQ.
 * Traduce el vocabulario de dominio a DTOs de mensajería.
 */
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
    public void publishStockReserve(UUID orderId, UUID userId, List<StockItem> items) {
        List<StockReserveItem> dtoItems = items.stream()
                .map(i -> new StockReserveItem(i.eventId(), i.ticketTypeId(), i.quantity()))
                .toList();
        StockReserveCommand command = new StockReserveCommand(orderId, userId, dtoItems);
        log.info("[PUBLISH] stock.reserve → orderId={}", orderId);
        rabbitTemplate.convertAndSend(exchange, rkStockReserve, command);
    }

    @Override
    public void publishOrderConfirmed(UUID orderId, UUID userId, BigDecimal totalAmount,
                                       String paymentMethodId, List<TicketData> tickets) {
        List<OrderConfirmedEvent.ConfirmedTicket> dtoTickets = tickets.stream()
                .map(t -> new OrderConfirmedEvent.ConfirmedTicket(
                        t.ticketId(), t.eventId(), t.ticketTypeId(), t.qrCode()))
                .toList();
        OrderConfirmedEvent event = new OrderConfirmedEvent(
                orderId, userId, totalAmount, paymentMethodId, dtoTickets);
        log.info("[PUBLISH] order.confirmed → orderId={}", orderId);
        rabbitTemplate.convertAndSend(exchange, rkOrderConfirmed, event);
    }

    @Override
    public void publishOrderCancelled(UUID orderId, UUID userId, String reason) {
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, userId, reason);
        log.info("[PUBLISH] order.cancelled → orderId={}, reason={}", orderId, reason);
        rabbitTemplate.convertAndSend(exchange, rkOrderCancelled, event);
    }
}
