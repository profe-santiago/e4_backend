package com.tickets.notification_service.notification.infrastructure.messaging.consumer;

import com.tickets.notification_service.notification.application.SendPaymentCompletedNotificationUseCase;
import com.tickets.notification_service.notification.application.dto.SendPaymentCompletedCommand;
import com.tickets.notification_service.notification.infrastructure.messaging.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentCompletedConsumer.class);

    private final SendPaymentCompletedNotificationUseCase useCase;

    public PaymentCompletedConsumer(SendPaymentCompletedNotificationUseCase useCase) {
        this.useCase = useCase;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.payment-completed}")
    public void handle(PaymentCompletedEvent event) {
        log.info("[CONSUME] payment.completed → orderId={}, userId={}", event.getOrderId(), event.getUserId());

        useCase.execute(new SendPaymentCompletedCommand(
                event.getOrderId(),
                event.getUserId(),
                event.getPaymentId(),
                event.getStripePaymentIntentId()
        ));
    }
}
