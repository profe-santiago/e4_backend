package com.tickets.ticket_service.order.application;

import com.tickets.ticket_service.order.application.dto.CreateOrderCommand;
import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderEventPublisher;
import com.tickets.ticket_service.order.domain.OrderItem;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.shared.UseCase;

import java.util.List;

/**
 * Caso de uso: crear una orden y publicar el comando de reserva de stock.
 * Inicia el flujo Saga coreografiado.
 */
@UseCase
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public CreateOrderUseCase(OrderRepository orderRepository,
                               OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public Order execute(CreateOrderCommand command) {
        List<OrderItem> items = command.items().stream()
                .map(i -> OrderItem.create(i.eventId(), i.ticketTypeId(), i.quantity()))
                .toList();

        Order order = Order.create(command.userId(), command.paymentMethodId(), items);
        Order saved = orderRepository.save(order);

        // Publica el comando asíncrono de reserva de stock — no bloquea
        List<OrderEventPublisher.StockItem> stockItems = saved.getItems().stream()
                .map(i -> new OrderEventPublisher.StockItem(
                        i.getEventId(), i.getTicketTypeId(), i.getQuantity()))
                .toList();

        eventPublisher.publishStockReserve(saved.getId(), saved.getUserId(), stockItems);

        return saved;
    }
}
