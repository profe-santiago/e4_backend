package com.tickets.notification_service.notification.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SendOrderConfirmedCommand(
        UUID orderId,
        UUID userId,
        BigDecimal totalAmount,
        List<ConfirmedTicket> tickets
) {
    public record ConfirmedTicket(UUID ticketId, UUID eventId, Long ticketTypeId, String qrCode) {
    }
}
