package com.tickets.payment_service.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command de entrada para el caso de uso ProcessPaymentUseCase.
 *
 * Es un record inmutable con los datos necesarios para procesar un pago.
 * El consumidor de RabbitMQ actúa como anti-corruption layer:
 * extrae sólo los campos relevantes del OrderConfirmedEvent y construye este command,
 * filtrando ruido externo (e.g. la lista de tickets que no le compete a pagos).
 */
public record ProcessPaymentCommand(
        UUID orderId,
        UUID userId,
        BigDecimal amount,
        String currency,
        String paymentIntentId
) {
}
