package com.tickets.payment_service.payment.domain.port;

import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.Payment;
import com.tickets.payment_service.payment.domain.UserId;

/**
 * Puerto secundario (salida) para publicación de eventos de dominio.
 *
 * El dominio define QUÉ eventos deben publicarse; la infraestructura
 * (RabbitPaymentEventPublisher) decide cómo y dónde publicarlos.
 */
public interface PaymentEventPort {

    void publishPaymentCompleted(Payment payment);

    void publishPaymentFailed(OrderId orderId, UserId userId, String reason);

    void publishRefundCompleted(OrderId orderId, UserId userId);

    void publishRefundFailed(OrderId orderId, UserId userId, String reason);
}
