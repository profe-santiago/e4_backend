package com.tickets.ticket_service.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * payment-service publica esto cuando el cobro con Stripe es exitoso.
 * notification-service puede consumir esto para enviar confirmación al usuario.
 * Alias RabbitMQ: "PaymentCompletedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    private UUID orderId;
    private UUID userId;
    private UUID paymentId;
    private String stripePaymentIntentId;
}
