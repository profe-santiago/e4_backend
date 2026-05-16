package com.tickets.payment_service.payment.infrastructure.rest.dto;

public record CreatePaymentIntentResponse(String clientSecret, String paymentIntentId) {}
