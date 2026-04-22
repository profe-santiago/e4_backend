package com.tickets.notification_service.notification.application.dto;

import java.util.UUID;

public record SendPaymentCompletedCommand(
        UUID orderId,
        UUID userId,
        UUID paymentId,
        String stripePaymentIntentId
) {
}
