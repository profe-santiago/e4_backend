package com.tickets.ticket_service.order.infrastructure.rest;

import com.tickets.ticket_service.order.application.dto.CreateOrderCommand;
import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderItem;
import com.tickets.ticket_service.order.infrastructure.rest.dto.CreateOrderRequest;
import com.tickets.ticket_service.order.infrastructure.rest.dto.OrderItemResponse;
import com.tickets.ticket_service.order.infrastructure.rest.dto.OrderResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class OrderRestMapper {

    public CreateOrderCommand toCommand(CreateOrderRequest request, UUID userId) {
        List<CreateOrderCommand.OrderItemData> items = request.getItems().stream()
                .map(i -> new CreateOrderCommand.OrderItemData(
                        i.getEventId(), i.getTicketTypeId(), i.getQuantity()))
                .toList();
        return new CreateOrderCommand(userId, request.getPaymentIntentId(), items);
    }

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .paymentIntentId(order.getPaymentIntentId())
                .items(order.getItems().stream().map(this::toItemResponse).toList())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        return OrderItemResponse.builder()
                .id(item.getId())
                .eventId(item.getEventId())
                .ticketTypeId(item.getTicketTypeId())
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .build();
    }
}
