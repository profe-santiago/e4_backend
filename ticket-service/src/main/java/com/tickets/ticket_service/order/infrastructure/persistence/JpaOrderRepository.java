package com.tickets.ticket_service.order.infrastructure.persistence;

import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.shared.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository springData;
    private final OrderPersistenceMapper mapper;

    public JpaOrderRepository(SpringDataOrderRepository springData,
                               OrderPersistenceMapper mapper) {
        this.springData = springData;
        this.mapper = mapper;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return springData.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Order> findByIdWithItems(UUID id) {
        return springData.findByIdWithItems(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Order> findByUserId(UUID userId, int page, int size) {
        Page<OrderJpaEntity> result = springData.findAllByUserId(userId, PageRequest.of(page, size));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                page,
                size
        );
    }

    @Override
    public Order save(Order order) {
        return mapper.toDomain(springData.save(mapper.toJpaEntity(order)));
    }
}
