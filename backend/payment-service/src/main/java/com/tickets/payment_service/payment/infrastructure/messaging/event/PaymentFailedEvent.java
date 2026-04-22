package com.tickets.payment_service.payment.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Evento publicado hacia ticket-service cuando el cobro es rechazado.
 * Routing key: payment.failed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private UUID orderId;
    private UUID userId;
    private String reason;
}
