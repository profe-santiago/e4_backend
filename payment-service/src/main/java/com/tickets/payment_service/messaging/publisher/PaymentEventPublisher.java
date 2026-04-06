package com.tickets.payment_service.messaging.publisher;

import com.tickets.payment_service.messaging.event.PaymentCompletedEvent;
import com.tickets.payment_service.messaging.event.PaymentFailedEvent;

/**
 * Puerto de salida para publicar eventos de pago a RabbitMQ.
 * Desacopla el dominio del broker concreto — DIP aplicado.
 */
public interface PaymentEventPublisher {
    void publishPaymentCompleted(PaymentCompletedEvent event);
    void publishPaymentFailed(PaymentFailedEvent event);
}
