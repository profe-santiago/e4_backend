package com.tickets.notification_service.notification.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de mensajería — espejo del evento publicado por payment-service.
 * Alias RabbitMQ: "PaymentCompletedEvent" — debe coincidir con el publisher.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentCompletedEvent {

    private UUID orderId;
    private UUID userId;
    private UUID paymentId;
    private String stripePaymentIntentId;
}
