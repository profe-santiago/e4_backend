package com.tickets.payment_service.payment.domain.port;

import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.Payment;
import com.tickets.payment_service.payment.domain.PaymentId;

import java.util.Optional;

/**
 * Puerto secundario (salida) de persistencia.
 *
 * El dominio define el contrato; la infraestructura (JpaPaymentRepositoryAdapter)
 * lo implementa. El dominio nunca sabe que existe JPA.
 */
public interface PaymentRepository {

    Payment save(Payment payment);

    boolean existsByOrderId(OrderId orderId);

    Optional<Payment> findByOrderId(OrderId orderId);

    Optional<Payment> findById(PaymentId id);
}
