package com.tickets.payment_service.payment.infrastructure.persistence;

import com.tickets.payment_service.payment.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA de pagos.
 *
 * Vive en infraestructura, no en el dominio. El dominio tiene su propio
 * Payment.java (POJO puro). El mapper convierte entre ambos mundos.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
class PaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "payment_method_id")
    private String paymentMethodId;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
