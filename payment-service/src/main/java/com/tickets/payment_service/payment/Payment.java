package com.tickets.payment_service.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // Moneda local por defecto (MXN para México)
    @Column(length = 10, nullable = false)
    private String currency = "MXN";

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    // Ej: "CREDIT_CARD", "DEBIT_CARD", "TRANSFER"
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    // ID de transacción devuelto por el proveedor de pagos
    @Column(name = "transaction_id", length = 255, unique = true)
    private String transactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
