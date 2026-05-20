package com.tickets.notification_service.notification.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de mensajeria — espejo del evento publicado por payment-service.
 * Alias RabbitMQ: "RefundCompletedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundCompletedEvent {
    private UUID orderId;
    private UUID userId;
}
