package com.tickets.payment_service.payment.infrastructure.persistence;

import com.tickets.payment_service.payment.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA de pagos.
 *
 * Implementa Persistable<UUID> para que Spring Data JPA llame persist() (INSERT)
 * en entidades nuevas y merge() (UPDATE) en entidades existentes, evitando
 * StaleObjectStateException cuando el ID es asignado manualmente.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
class PaymentJpaEntity implements Persistable<UUID> {

    @Id
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

    @Column(name = "payment_method")
    private String paymentMethodId;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Indica si la entidad es nueva (INSERT) o existente (UPDATE).
     * Una entidad es nueva si aún no tiene created_at (se asigna en el dominio al crear).
     * Tras cargar desde BD (@PostLoad), updatedAt puede ser null pero createdAt != null.
     */
    @Transient
    private boolean isNew = true;

    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }
}
