package com.tickets.notification_service.notification.infrastructure.messaging.consumer;

import com.tickets.notification_service.notification.application.SendRefundCompletedNotificationUseCase;
import com.tickets.notification_service.notification.application.dto.SendRefundCompletedCommand;
import com.tickets.notification_service.notification.infrastructure.messaging.event.RefundCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RefundCompletedConsumer {

    private static final Logger log = LoggerFactory.getLogger(RefundCompletedConsumer.class);

    private final SendRefundCompletedNotificationUseCase useCase;

    public RefundCompletedConsumer(SendRefundCompletedNotificationUseCase useCase) {
        this.useCase = useCase;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.refund-completed}")
    public void handle(RefundCompletedEvent event) {
        log.info("[CONSUME] refund.completed → orderId={}, userId={}", event.getOrderId(), event.getUserId());
        useCase.execute(new SendRefundCompletedCommand(event.getOrderId(), event.getUserId()));
    }
}
