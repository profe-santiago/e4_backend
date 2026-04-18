package com.tickets.payment_service.payment.infrastructure.messaging.consumer;

import com.tickets.payment_service.payment.application.RefundPaymentUseCase;
import com.tickets.payment_service.payment.infrastructure.messaging.event.RefundInitiatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: consume RefundInitiatedEvent publicado por ticket-service.
 *
 * Delega al RefundPaymentUseCase sin lógica de negocio propia.
 */
@Component
public class RefundInitiatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(RefundInitiatedConsumer.class);

    private final RefundPaymentUseCase refundPaymentUseCase;

    public RefundInitiatedConsumer(RefundPaymentUseCase refundPaymentUseCase) {
        this.refundPaymentUseCase = refundPaymentUseCase;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.refund-initiated}")
    public void handle(RefundInitiatedEvent event) {
        log.info("[RMQ] Received RefundInitiatedEvent: orderId={}, userId={}",
                event.getOrderId(), event.getUserId());
        refundPaymentUseCase.execute(event.getOrderId(), event.getUserId());
    }
}
