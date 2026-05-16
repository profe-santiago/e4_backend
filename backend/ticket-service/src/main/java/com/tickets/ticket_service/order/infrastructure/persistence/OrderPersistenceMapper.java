package com.tickets.ticket_service.order.infrastructure.persistence;

import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper entre Order/OrderItem (dominio) y sus JPA entities.
 * Bean de Spring — inyectable y testeable.
 */
@Component
public class OrderPersistenceMapper {

    public Order toDomain(OrderJpaEntity entity) {
        Order order = new Order();
        order.setId(entity.getId());
        order.setUserId(entity.getUserId());
        order.setStatus(entity.getStatus());
        order.setTotalAmount(entity.getTotalAmount());
        order.setPaymentIntentId(entity.getPaymentIntentId());
        order.setCreatedAt(entity.getCreatedAt());
        order.setUpdatedAt(entity.getUpdatedAt());

        List<OrderItem> items = entity.getItems().stream()
                .map(this::itemToDomain)
                .toList();
        order.setItems(items);

        return order;
    }

    public OrderItem itemToDomain(OrderItemJpaEntity entity) {
        OrderItem item = new OrderItem();
        item.setId(entity.getId());
        item.setEventId(entity.getEventId());
        item.setTicketTypeId(entity.getTicketTypeId());
        item.setQuantity(entity.getQuantity());
        item.setUnitPrice(entity.getUnitPrice());
        return item;
    }

    public OrderJpaEntity toJpaEntity(Order domain) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setStatus(domain.getStatus());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setPaymentIntentId(domain.getPaymentIntentId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        List<OrderItemJpaEntity> items = domain.getItems().stream()
                .map(item -> itemToJpaEntity(item, entity))
                .toList();
        entity.getItems().clear();
        entity.getItems().addAll(items);

        return entity;
    }

    private OrderItemJpaEntity itemToJpaEntity(OrderItem domain, OrderJpaEntity orderEntity) {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.setId(domain.getId());
        entity.setOrder(orderEntity);
        entity.setEventId(domain.getEventId());
        entity.setTicketTypeId(domain.getTicketTypeId());
        entity.setQuantity(domain.getQuantity());
        entity.setUnitPrice(domain.getUnitPrice());
        return entity;
    }
}
