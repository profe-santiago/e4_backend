package com.tickets.payment_service.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Interfaz Spring Data JPA — detalle de implementación interno de la capa de persistencia.
 * Package-private: solo la usa JpaPaymentRepositoryAdapter.
 */
interface JpaPaymentRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    boolean existsByOrderId(UUID orderId);

    Optional<PaymentJpaEntity> findByOrderId(UUID orderId);
}
