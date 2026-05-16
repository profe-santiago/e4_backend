package com.tickets.payment_service.payment.domain;

public record CreateIntentResult(String clientSecret, String paymentIntentId) {}
