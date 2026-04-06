package com.tickets.ticket_service.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * payment-service publica esto cuando el cobro con Stripe falla.
 * ticket-service lo consume para marcar la orden como FAILED y liberar el stock.
 * Alias RabbitMQ: "PaymentFailedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private UUID orderId;
    private UUID userId;
    private String reason;
}
