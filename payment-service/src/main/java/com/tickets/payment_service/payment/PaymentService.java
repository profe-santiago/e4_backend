package com.tickets.payment_service.payment;

import com.tickets.payment_service.messaging.event.OrderConfirmedEvent;
import com.tickets.payment_service.payment.dto.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    /**
     * Procesa el cobro para una orden confirmada.
     * Idempotente: si ya existe un pago para el orderId, retorna sin hacer nada.
     * Publica PaymentCompletedEvent o PaymentFailedEvent según el resultado.
     */
    void processPayment(OrderConfirmedEvent event);

    PaymentResponse findByOrderId(UUID orderId);

    PaymentResponse findById(UUID paymentId);
}
