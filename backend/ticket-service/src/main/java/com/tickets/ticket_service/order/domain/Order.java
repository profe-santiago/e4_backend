package com.tickets.ticket_service.order.domain;

import com.tickets.ticket_service.exception.InvalidOrderStateException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root del bounded context Order.
 * POJO puro — sin Spring, sin JPA, sin ninguna librería de infraestructura.
 *
 * Modelo rico: la lógica de negocio vive aquí, no en el UseCase.
 * Invariante: el aggregate controla el acceso a sus OrderItems.
 */
public class Order {

    private UUID id;
    private UUID userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String paymentIntentId;
    private List<OrderItem> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order() {}

    // ─── Factory method ───────────────────────────────────────────────────

    public static Order create(UUID userId, String paymentIntentId, List<OrderItem> items) {
        Order order = new Order();
        order.userId = userId;
        order.paymentIntentId = paymentIntentId;
        order.status = OrderStatus.PENDING;
        order.totalAmount = BigDecimal.ZERO; // se actualiza al confirmar
        order.items = new ArrayList<>(items);
        order.createdAt = LocalDateTime.now();
        return order;
    }

    // ─── Rich behavior ────────────────────────────────────────────────────

    /**
     * Confirma la orden: actualiza precios reales devueltos por event-service
     * y calcula el total. Valida la transición de estado.
     */
    public void confirm(List<StockConfirmationItem> reservedItems) {
        if (!status.canTransitionTo(OrderStatus.CONFIRMED)) {
            throw new InvalidOrderStateException(status, OrderStatus.CONFIRMED);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (StockConfirmationItem reserved : reservedItems) {
            for (OrderItem item : items) {
                if (item.matchesTicketType(reserved.eventId(), reserved.ticketTypeId())) {
                    item.applyPrice(reserved.unitPrice());
                    total = total.add(reserved.unitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
        }
        this.totalAmount = total;
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    /** Marca la orden como FAILED (sin stock o pago rechazado). */
    public void fail() {
        if (!status.canTransitionTo(OrderStatus.FAILED)) {
            throw new InvalidOrderStateException(status, OrderStatus.FAILED);
        }
        this.status = OrderStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /** Cancelación iniciada por el usuario. */
    public void cancel() {
        if (!status.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new InvalidOrderStateException(status, OrderStatus.CANCELLED);
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /** Inicia el flujo de reembolso: CONFIRMED → REFUND_PENDING. */
    public void requestRefund() {
        if (!status.canTransitionTo(OrderStatus.REFUND_PENDING)) {
            throw new InvalidOrderStateException(status, OrderStatus.REFUND_PENDING);
        }
        this.status = OrderStatus.REFUND_PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    /** Reembolso aprobado en Stripe: REFUND_PENDING → REFUNDED. */
    public void refund() {
        if (!status.canTransitionTo(OrderStatus.REFUNDED)) {
            throw new InvalidOrderStateException(status, OrderStatus.REFUNDED);
        }
        this.status = OrderStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();
    }

    /** Reembolso fallido: REFUND_PENDING → CONFIRMED (permite reintentar). */
    public void failRefund() {
        if (!status.canTransitionTo(OrderStatus.CONFIRMED)) {
            throw new InvalidOrderStateException(status, OrderStatus.CONFIRMED);
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Getters ─────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items != null ? items : List.of()); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ─── Setters (solo para el mapper de persistencia) ───────────────────

    public void setId(UUID id) { this.id = id; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
    public void setItems(List<OrderItem> items) { this.items = new ArrayList<>(items); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
