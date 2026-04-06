package com.tickets.notification_service.messaging.consumer;

import com.tickets.notification_service.messaging.event.PaymentCompletedEvent;
import com.tickets.notification_service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume el evento payment.completed publicado por payment-service.
 * Notifica al usuario que su pago fue procesado y sus tickets están listos.
 */
@Component
public class PaymentCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedConsumer.class);

    private final NotificationService notificationService;

    public PaymentCompletedConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.payment-completed}")
    public void handle(PaymentCompletedEvent event) {
        log.info("[CONSUME] payment.completed → orderId={}, userId={}, paymentId={}",
                event.getOrderId(), event.getUserId(), event.getPaymentId());
        notificationService.processPaymentCompleted(event);
    }
}
