package com.tickets.payment_service.payment.infrastructure.persistence;

import com.tickets.payment_service.payment.domain.Money;
import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.Payment;
import com.tickets.payment_service.payment.domain.PaymentId;
import com.tickets.payment_service.payment.domain.UserId;
import org.springframework.stereotype.Component;

@Component
class PaymentPersistenceMapper {

    PaymentJpaEntity toJpaEntity(Payment domain) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setId(domain.getId().value());
        entity.setOrderId(domain.getOrderId().value());
        entity.setUserId(domain.getUserId().value());
        entity.setAmount(domain.getAmount().amount());
        entity.setCurrency(domain.getAmount().currency());
        entity.setStatus(domain.getStatus());
        entity.setPaymentIntentId(domain.getPaymentIntentId());
        entity.setTransactionId(domain.getTransactionId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    Payment toDomain(PaymentJpaEntity entity) {
        return Payment.reconstitute(
                PaymentId.of(entity.getId()),
                OrderId.of(entity.getOrderId()),
                UserId.of(entity.getUserId()),
                Money.of(entity.getAmount(), entity.getCurrency()),
                entity.getPaymentIntentId(),
                entity.getStatus(),
                entity.getTransactionId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
