package com.tickets.ticket_service.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * ticket-service publica esto cuando una orden queda confirmada y los tickets generados.
 * payment-service y notification-service consumirán este evento.
 * Alias RabbitMQ: "OrderConfirmedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {
    private UUID orderId;
    private UUID userId;
    private BigDecimal totalAmount;
    private List<ConfirmedTicket> tickets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmedTicket {
        private UUID ticketId;
        private UUID eventId;
        private Long ticketTypeId;
        private String qrCode;
    }
}
