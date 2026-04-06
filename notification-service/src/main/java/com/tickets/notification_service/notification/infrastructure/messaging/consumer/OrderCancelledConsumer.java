package com.tickets.notification_service.notification.infrastructure.messaging.consumer;

import com.tickets.notification_service.notification.application.SendOrderCancelledNotificationUseCase;
import com.tickets.notification_service.notification.application.dto.SendOrderCancelledCommand;
import com.tickets.notification_service.notification.infrastructure.messaging.event.OrderCancelledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCancelledConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelledConsumer.class);

    private final SendOrderCancelledNotificationUseCase useCase;

    public OrderCancelledConsumer(SendOrderCancelledNotificationUseCase useCase) {
        this.useCase = useCase;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.order-cancelled}")
    public void handle(OrderCancelledEvent event) {
        log.info("[CONSUME] order.cancelled → orderId={}, userId={}", event.getOrderId(), event.getUserId());

        useCase.execute(new SendOrderCancelledCommand(
                event.getOrderId(),
                event.getUserId(),
                event.getReason()
        ));
    }
}
