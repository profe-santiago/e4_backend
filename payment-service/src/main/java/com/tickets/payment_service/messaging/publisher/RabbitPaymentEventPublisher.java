package com.tickets.payment_service.messaging.publisher;

import com.tickets.payment_service.messaging.event.PaymentCompletedEvent;
import com.tickets.payment_service.messaging.event.PaymentFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitPaymentEventPublisher implements PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitPaymentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-keys.payment-completed}")
    private String rkPaymentCompleted;

    @Value("${app.rabbitmq.routing-keys.payment-failed}")
    private String rkPaymentFailed;

    public RabbitPaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("[PUBLISH] payment.completed → orderId={}", event.getOrderId());
        rabbitTemplate.convertAndSend(exchange, rkPaymentCompleted, event);
    }

    @Override
    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("[PUBLISH] payment.failed → orderId={}, reason={}", event.getOrderId(), event.getReason());
        rabbitTemplate.convertAndSend(exchange, rkPaymentFailed, event);
    }
}
