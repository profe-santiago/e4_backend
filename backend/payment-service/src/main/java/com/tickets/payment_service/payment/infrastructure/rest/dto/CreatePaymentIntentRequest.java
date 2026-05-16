package com.tickets.payment_service.payment.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePaymentIntentRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String currency
) {}
