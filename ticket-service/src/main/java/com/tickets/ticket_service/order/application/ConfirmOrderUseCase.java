package com.tickets.ticket_service.order.application;

import com.tickets.ticket_service.exception.OrderNotFoundException;
import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderEventPublisher;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.order.domain.StockConfirmationItem;
import com.tickets.ticket_service.shared.UseCase;
import com.tickets.ticket_service.ticket.application.GenerateTicketsUseCase;
import com.tickets.ticket_service.ticket.application.dto.GenerateTicketsCommand;
import com.tickets.ticket_service.ticket.application.dto.GeneratedTicketData;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: confirmar una orden después de que event-service confirma el stock.
 *
 * Flujo Saga:
 *   1. Actualiza precios reales en los ítems (behavior en Order.confirm)
 *   2. Genera los tickets físicos (delegado a GenerateTicketsUseCase)
 *   3. Publica OrderConfirmedEvent → payment-service cobra
 */
@UseCase
public class ConfirmOrderUseCase {

    private final OrderRepository orderRepository;
    private final GenerateTicketsUseCase generateTickets;
    private final OrderEventPublisher eventPublisher;

    public ConfirmOrderUseCase(OrderRepository orderRepository,
                                GenerateTicketsUseCase generateTickets,
                                OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.generateTickets = generateTickets;
        this.eventPublisher = eventPublisher;
    }

    public Order execute(UUID orderId, List<StockConfirmationItem> reservedItems) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // La lógica de precios y transición vive en el dominio
        order.confirm(reservedItems);
        Order saved = orderRepository.save(order);

        // Genera los tickets — cross-feature pero sin pasar la JPA entity
        GenerateTicketsCommand ticketCommand = buildTicketCommand(saved);
        List<GeneratedTicketData> generated = generateTickets.execute(ticketCommand);

        // Publica OrderConfirmedEvent — payment-service consume esto
        List<OrderEventPublisher.TicketData> ticketData = generated.stream()
                .map(g -> new OrderEventPublisher.TicketData(
                        g.ticketId(), g.eventId(), g.ticketTypeId(), g.qrCode()))
                .toList();

        eventPublisher.publishOrderConfirmed(
                saved.getId(), saved.getUserId(), saved.getTotalAmount(),
                saved.getPaymentMethodId(), ticketData);

        return saved;
    }

    private GenerateTicketsCommand buildTicketCommand(Order order) {
        List<GenerateTicketsCommand.OrderItemData> items = order.getItems().stream()
                .map(item -> new GenerateTicketsCommand.OrderItemData(
                        item.getId(), item.getEventId(), item.getTicketTypeId(), item.getQuantity()))
                .toList();

        return new GenerateTicketsCommand(order.getId(), order.getUserId(), items);
    }
}
