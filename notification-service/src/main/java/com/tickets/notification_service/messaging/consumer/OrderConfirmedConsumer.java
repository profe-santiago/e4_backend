package com.tickets.notification_service.messaging.consumer;

import com.tickets.notification_service.messaging.event.OrderConfirmedEvent;
import com.tickets.notification_service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume el evento order.confirmed publicado por ticket-service.
 * Notifica al usuario que su orden fue recibida y está siendo procesada.
 */
@Component
public class OrderConfirmedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmedConsumer.class);

    private final NotificationService notificationService;

    public OrderConfirmedConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.order-confirmed}")
    public void handle(OrderConfirmedEvent event) {
        log.info("[CONSUME] order.confirmed → orderId={}, userId={}",
                event.getOrderId(), event.getUserId());
        notificationService.processOrderConfirmed(event);
    }
}
