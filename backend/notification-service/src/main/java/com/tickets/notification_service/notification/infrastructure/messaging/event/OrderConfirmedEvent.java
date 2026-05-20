package com.tickets.notification_service.notification.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO de mensajería — espejo del evento publicado por ticket-service.
 * Alias RabbitMQ: "OrderConfirmedEvent" — debe coincidir con el publisher.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderConfirmedEvent {

    private UUID orderId;
    private UUID userId;
    private BigDecimal totalAmount;
    private String paymentIntentId;
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
