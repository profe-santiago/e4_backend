package com.tickets.ticket_service.order.application;

import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.shared.PageResult;
import com.tickets.ticket_service.shared.UseCase;

import java.util.UUID;

@UseCase
public class ListMyOrdersUseCase {

    private final OrderRepository orderRepository;

    public ListMyOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public PageResult<Order> execute(UUID userId, int page, int size) {
        return orderRepository.findByUserId(userId, page, size);
    }
}
