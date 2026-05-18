package com.tickets.ticket_service.order.infrastructure.persistence;

import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.shared.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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
        Page<OrderJpaEntity> result = springData.findAllByUserId(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                page,
                size
        );
    }

    @Override
    public List<Order> findExpiredPendingOrders(LocalDateTime expirationThreshold) {
        return springData.findExpiredPendingOrders(expirationThreshold)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByPaymentIntentId(String paymentIntentId) {
        return springData.existsByPaymentIntentId(paymentIntentId);
    }

    @Override
    public Order save(Order order) {
        return mapper.toDomain(springData.save(mapper.toJpaEntity(order)));
    }
}
