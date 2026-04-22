package com.tickets.payment_service.payment.domain;

import java.util.UUID;

public record PaymentId(UUID value) {

    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID());
    }

    public static PaymentId of(UUID value) {
        return new PaymentId(value);
    }
}
