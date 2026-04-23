package com.tickets.notification_service.notification.application.dto;

import com.tickets.notification_service.notification.domain.UserId;

public record GetUserNotificationsQuery(
        UserId userId,
        int page,
        int size
) {
}
