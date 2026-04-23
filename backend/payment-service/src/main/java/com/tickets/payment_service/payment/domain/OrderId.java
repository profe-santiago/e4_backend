package com.tickets.payment_service.payment.domain;

import java.util.UUID;

public record OrderId(UUID value) {

    public static OrderId of(UUID value) {
        return new OrderId(value);
    }
}
