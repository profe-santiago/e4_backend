package com.tickets.ticket_service.order.infrastructure.messaging;

import com.tickets.ticket_service.order.application.FailOrderUseCase;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.PaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador primario: escucha rechazos de pago desde payment-service.
 * Cuando llega → marca la orden como FAILED (lo que dispara OrderCancelledEvent
 * para que event-service libere el stock).
 */
@Component
public class PaymentFailedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentFailedConsumer.class);

    private final FailOrderUseCase failOrder;

    public PaymentFailedConsumer(FailOrderUseCase failOrder) {
        this.failOrder = failOrder;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.payment-failed}")
    public void handle(PaymentFailedEvent event) {
        log.info("[CONSUME] payment.failed → orderId={}, reason={}", event.getOrderId(), event.getReason());
        failOrder.execute(event.getOrderId(), "Pago rechazado: " + event.getReason());
    }
}
