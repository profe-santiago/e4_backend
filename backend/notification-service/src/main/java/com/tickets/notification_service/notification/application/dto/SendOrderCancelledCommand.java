package com.tickets.notification_service.notification.application.dto;

import java.util.UUID;

public record SendOrderCancelledCommand(
        UUID orderId,
        UUID userId,
        String reason
) {
}
