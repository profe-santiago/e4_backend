package com.tickets.payment_service.payment.domain;

import java.time.LocalDateTime;

/**
 * Aggregate Root del dominio de pagos.
 *
 * POJO puro — sin @Entity, sin Spring, sin ninguna librería de infraestructura.
 * La lógica de transición de estado (confirm/reject) vive aquí,
 * no en los use cases ni en la infraestructura.
 */
public class Payment {

    private PaymentId id;
    private OrderId orderId;
    private UserId userId;
    private Money amount;
    private String paymentMethodId;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Payment() {
    }

    // ── Factory: nuevo pago ──────────────────────────────────────────────────

    public static Payment create(OrderId orderId, UserId userId, Money amount, String paymentMethodId) {
        Payment p = new Payment();
        p.id = PaymentId.generate();
        p.orderId = orderId;
        p.userId = userId;
        p.amount = amount;
        p.paymentMethodId = paymentMethodId;
        p.status = PaymentStatus.PENDING;
        p.createdAt = LocalDateTime.now();
        return p;
    }

    // ── Factory: reconstituir desde persistencia ─────────────────────────────

    public static Payment reconstitute(PaymentId id,
                                       OrderId orderId,
                                       UserId userId,
                                       Money amount,
                                       String paymentMethodId,
                                       PaymentStatus status,
                                       String transactionId,
                                       LocalDateTime createdAt,
                                       LocalDateTime updatedAt) {
        Payment p = new Payment();
        p.id = id;
        p.orderId = orderId;
        p.userId = userId;
        p.amount = amount;
        p.paymentMethodId = paymentMethodId;
        p.status = status;
        p.transactionId = transactionId;
        p.createdAt = createdAt;
        p.updatedAt = updatedAt;
        return p;
    }

    // ── Comportamiento de dominio ────────────────────────────────────────────

    /**
     * Confirma el pago luego de que el gateway reportó éxito.
     * Valida que la transición sea válida (sólo PENDING puede confirmarse).
     */
    public void confirm(String transactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot confirm payment in status %s — only PENDING payments can be confirmed".formatted(status));
        }
        this.status = PaymentStatus.APPROVED;
        this.transactionId = transactionId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Rechaza el pago. Puede ser por error del gateway o estado inesperado de Stripe.
     */
    public void reject(String reason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot reject payment in status %s — only PENDING payments can be rejected".formatted(status));
        }
        this.status = PaymentStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marca el pago como reembolsado luego de que Stripe confirmó el refund.
     * Solo pagos APPROVED pueden reembolsarse.
     */
    public void refund() {
        if (this.status != PaymentStatus.APPROVED) {
            throw new IllegalStateException(
                    "Cannot refund payment in status %s — only APPROVED payments can be refunded".formatted(status));
        }
        this.status = PaymentStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getters (sin setters: las transiciones van por métodos de dominio) ───

    public PaymentId getId() {
        return id;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public UserId getUserId() {
        return userId;
    }

    public Money getAmount() {
        return amount;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
