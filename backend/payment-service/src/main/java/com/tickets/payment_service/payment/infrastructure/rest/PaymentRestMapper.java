package com.tickets.payment_service.payment.infrastructure.rest;

import com.tickets.payment_service.payment.domain.Payment;
import com.tickets.payment_service.payment.infrastructure.rest.dto.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentRestMapper {

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId().value())
                .orderId(payment.getOrderId().value())
                .userId(payment.getUserId().value())
                .amount(payment.getAmount().amount())
                .currency(payment.getAmount().currency())
                .status(payment.getStatus())
                .paymentMethodId(payment.getPaymentMethodId())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
