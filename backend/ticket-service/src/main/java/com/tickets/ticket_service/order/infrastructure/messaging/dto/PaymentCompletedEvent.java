package com.tickets.ticket_service.order.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * payment-service publica esto cuando el cobro es exitoso.
 * notification-service lo consume para enviar confirmación al usuario.
 * Type alias RabbitMQ: "PaymentCompletedEvent"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    private UUID orderId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
}
