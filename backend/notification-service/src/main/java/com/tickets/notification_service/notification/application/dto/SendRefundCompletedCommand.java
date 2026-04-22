package com.tickets.notification_service.notification.application.dto;

import java.util.UUID;

public record SendRefundCompletedCommand(
        UUID orderId,
        UUID userId
) {}
