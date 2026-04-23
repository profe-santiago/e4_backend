package com.tickets.payment_service.payment.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Evento entrante publicado por ticket-service cuando una orden es confirmada.
 *
 * El consumer actúa como anti-corruption layer: extrae sólo los campos
 * relevantes para el pago y construye un ProcessPaymentCommand con ellos.
 * La lista de tickets no le compete a payment-service y se filtra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {
    private UUID orderId;
    private UUID userId;
    private BigDecimal totalAmount;
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
