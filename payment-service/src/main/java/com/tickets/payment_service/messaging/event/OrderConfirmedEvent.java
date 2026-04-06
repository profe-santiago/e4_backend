package com.tickets.payment_service.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Espejo del evento publicado por ticket-service.
 * Alias RabbitMQ: "OrderConfirmedEvent" — debe coincidir con el alias en ticket-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {
    private UUID orderId;
    private UUID userId;
    private BigDecimal totalAmount;
    /** ID del método de pago de Stripe (pm_xxxxx) para ejecutar el cobro */
    private String paymentMethodId;
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
