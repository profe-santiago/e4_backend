package com.tickets.notification_service.notification.infrastructure.messaging.consumer;

import com.tickets.notification_service.notification.application.SendRefundFailedNotificationUseCase;
import com.tickets.notification_service.notification.application.dto.SendRefundFailedCommand;
import com.tickets.notification_service.notification.infrastructure.messaging.event.RefundFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RefundFailedConsumer {

    private static final Logger log = LoggerFactory.getLogger(RefundFailedConsumer.class);

    private final SendRefundFailedNotificationUseCase useCase;

    public RefundFailedConsumer(SendRefundFailedNotificationUseCase useCase) {
        this.useCase = useCase;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.refund-failed}")
    public void handle(RefundFailedEvent event) {
        log.info("[CONSUME] refund.failed → orderId={}, userId={}", event.getOrderId(), event.getUserId());
        useCase.execute(new SendRefundFailedCommand(event.getOrderId(), event.getUserId(), event.getReason()));
    }
}
