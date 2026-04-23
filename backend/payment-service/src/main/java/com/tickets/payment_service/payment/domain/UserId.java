package com.tickets.payment_service.payment.domain;

import java.util.UUID;

public record UserId(UUID value) {

    public static UserId of(UUID value) {
        return new UserId(value);
    }
}
