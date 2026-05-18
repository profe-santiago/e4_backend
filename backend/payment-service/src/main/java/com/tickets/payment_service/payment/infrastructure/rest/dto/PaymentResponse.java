package com.tickets.payment_service.payment.infrastructure.rest.dto;

import com.tickets.payment_service.payment.domain.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String paymentIntentId;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
