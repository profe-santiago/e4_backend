package com.tickets.ticket_service.order.infrastructure.messaging;

import com.tickets.ticket_service.order.domain.OrderEventPublisher;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.OrderCancelledEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.OrderConfirmedEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.OrderRefundedEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.RefundInitiatedEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.ReleaseStockItem;
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

    @Value("${app.rabbitmq.routing-keys.refund-initiated}")
    private String rkRefundInitiated;

    @Value("${app.rabbitmq.routing-keys.order-refunded}")
    private String rkOrderRefunded;

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
                                       String paymentIntentId, List<TicketData> tickets) {
        List<OrderConfirmedEvent.ConfirmedTicket> dtoTickets = tickets.stream()
                .map(t -> new OrderConfirmedEvent.ConfirmedTicket(
                        t.ticketId(), t.eventId(), t.ticketTypeId(), t.qrCode()))
                .toList();
        OrderConfirmedEvent event = new OrderConfirmedEvent(
                orderId, userId, totalAmount, paymentIntentId, dtoTickets);
        log.info("[PUBLISH] order.confirmed → orderId={}", orderId);
        rabbitTemplate.convertAndSend(exchange, rkOrderConfirmed, event);
    }

    @Override
    public void publishOrderCancelled(UUID orderId, UUID userId, String reason, List<StockReleaseItem> items) {
        List<ReleaseStockItem> dtoItems = items.stream()
                .map(i -> new ReleaseStockItem(i.eventId(), i.ticketTypeId(), i.quantity()))
                .toList();
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, userId, reason, dtoItems);
        log.info("[PUBLISH] order.cancelled → orderId={}, reason={}", orderId, reason);
        rabbitTemplate.convertAndSend(exchange, rkOrderCancelled, event);
    }

    @Override
    public void publishRefundInitiated(UUID orderId, UUID userId) {
        RefundInitiatedEvent event = new RefundInitiatedEvent(orderId, userId);
        log.info("[PUBLISH] refund.initiated → orderId={}", orderId);
        rabbitTemplate.convertAndSend(exchange, rkRefundInitiated, event);
    }

    @Override
    public void publishOrderRefunded(UUID orderId, UUID userId, List<StockReleaseItem> items) {
        List<ReleaseStockItem> dtoItems = items.stream()
                .map(i -> new ReleaseStockItem(i.eventId(), i.ticketTypeId(), i.quantity()))
                .toList();
        OrderRefundedEvent event = new OrderRefundedEvent(orderId, userId, dtoItems);
        log.info("[PUBLISH] order.refunded → orderId={}", orderId);
        rabbitTemplate.convertAndSend(exchange, rkOrderRefunded, event);
    }
}
