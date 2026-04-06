package com.tickets.payment_service.payment.infrastructure.persistence;

import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.Payment;
import com.tickets.payment_service.payment.domain.PaymentId;
import com.tickets.payment_service.payment.domain.port.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador que implementa el puerto de persistencia del dominio
 * usando Spring Data JPA como mecanismo subyacente.
 *
 * El dominio sólo conoce la interfaz PaymentRepository; este adaptador
 * es invisible para él (Dependency Inversion Principle en acción).
 */
@Repository
class JpaPaymentRepositoryAdapter implements PaymentRepository {

    private final JpaPaymentRepository jpaRepository;
    private final PaymentPersistenceMapper mapper;

    JpaPaymentRepositoryAdapter(JpaPaymentRepository jpaRepository, PaymentPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(payment));
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByOrderId(OrderId orderId) {
        return jpaRepository.existsByOrderId(orderId.value());
    }

    @Override
    public Optional<Payment> findByOrderId(OrderId orderId) {
        return jpaRepository.findByOrderId(orderId.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }
}
