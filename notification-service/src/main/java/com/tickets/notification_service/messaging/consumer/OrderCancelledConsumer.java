package com.tickets.notification_service.messaging.consumer;

import com.tickets.notification_service.messaging.event.OrderCancelledEvent;
import com.tickets.notification_service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume el evento order.cancelled publicado por ticket-service.
 * Notifica al usuario que su orden fue cancelada junto con el motivo.
 */
@Component
public class OrderCancelledConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelledConsumer.class);

    private final NotificationService notificationService;

    public OrderCancelledConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.order-cancelled}")
    public void handle(OrderCancelledEvent event) {
        log.info("[CONSUME] order.cancelled → orderId={}, userId={}, reason={}",
                event.getOrderId(), event.getUserId(), event.getReason());
        notificationService.processOrderCancelled(event);
    }
}
