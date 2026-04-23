package com.tickets.ticket_service.order.application;

import com.tickets.ticket_service.exception.OrderNotFoundException;
import com.tickets.ticket_service.exception.UnauthorizedActionException;
import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.shared.UseCase;

import java.util.UUID;

@UseCase
public class GetOrderByIdUseCase {

    private final OrderRepository orderRepository;

    public GetOrderByIdUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order execute(UUID orderId, UUID requesterId, boolean isAdmin) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!isAdmin && !order.getUserId().equals(requesterId)) {
            throw new UnauthorizedActionException("No tenés permisos para ver esta orden");
        }

        return order;
    }
}
