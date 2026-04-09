package com.tickets.notification_service.notification.domain;

import java.util.UUID;

public record NotificationId(UUID value) {

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID());
    }

    public static NotificationId of(UUID value) {
        return new NotificationId(value);
    }
}
