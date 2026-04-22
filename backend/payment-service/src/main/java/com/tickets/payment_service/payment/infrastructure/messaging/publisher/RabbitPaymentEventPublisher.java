package com.tickets.payment_service.payment.infrastructure.messaging.publisher;

import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.Payment;
import com.tickets.payment_service.payment.domain.UserId;
import com.tickets.payment_service.payment.domain.port.PaymentEventPort;
import com.tickets.payment_service.payment.infrastructure.messaging.event.PaymentCompletedEvent;
import com.tickets.payment_service.payment.infrastructure.messaging.event.PaymentFailedEvent;
import com.tickets.payment_service.payment.infrastructure.messaging.event.RefundCompletedEvent;
import com.tickets.payment_service.payment.infrastructure.messaging.event.RefundFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que implementa PaymentEventPort usando RabbitMQ.
 *
 * Convierte objetos de dominio a eventos de mensajería y los publica
 * al exchange compartido con los routing keys correspondientes.
 */
@Component
class RabbitPaymentEventPublisher implements PaymentEventPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitPaymentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String rkPaymentCompleted;
    private final String rkPaymentFailed;
    private final String rkRefundCompleted;
    private final String rkRefundFailed;

    RabbitPaymentEventPublisher(RabbitTemplate rabbitTemplate,
                                @Value("${app.rabbitmq.exchange}") String exchange,
                                @Value("${app.rabbitmq.routing-keys.payment-completed}") String rkPaymentCompleted,
                                @Value("${app.rabbitmq.routing-keys.payment-failed}") String rkPaymentFailed,
                                @Value("${app.rabbitmq.routing-keys.refund-completed}") String rkRefundCompleted,
                                @Value("${app.rabbitmq.routing-keys.refund-failed}") String rkRefundFailed) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.rkPaymentCompleted = rkPaymentCompleted;
        this.rkPaymentFailed = rkPaymentFailed;
        this.rkRefundCompleted = rkRefundCompleted;
        this.rkRefundFailed = rkRefundFailed;
    }

    @Override
    public void publishPaymentCompleted(Payment payment) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                payment.getOrderId().value(),
                payment.getUserId().value(),
                payment.getId().value(),
                payment.getTransactionId()
        );
        log.info("[RMQ] Publishing PaymentCompletedEvent: orderId={}", payment.getOrderId().value());
        rabbitTemplate.convertAndSend(exchange, rkPaymentCompleted, event);
    }

    @Override
    public void publishPaymentFailed(OrderId orderId, UserId userId, String reason) {
        PaymentFailedEvent event = new PaymentFailedEvent(
                orderId.value(),
                userId.value(),
                reason
        );
        log.info("[RMQ] Publishing PaymentFailedEvent: orderId={}", orderId.value());
        rabbitTemplate.convertAndSend(exchange, rkPaymentFailed, event);
    }

    @Override
    public void publishRefundCompleted(OrderId orderId, UserId userId) {
        RefundCompletedEvent event = new RefundCompletedEvent(orderId.value(), userId.value());
        log.info("[RMQ] Publishing RefundCompletedEvent: orderId={}", orderId.value());
        rabbitTemplate.convertAndSend(exchange, rkRefundCompleted, event);
    }

    @Override
    public void publishRefundFailed(OrderId orderId, UserId userId, String reason) {
        RefundFailedEvent event = new RefundFailedEvent(orderId.value(), userId.value(), reason);
        log.warn("[RMQ] Publishing RefundFailedEvent: orderId={}, reason={}", orderId.value(), reason);
        rabbitTemplate.convertAndSend(exchange, rkRefundFailed, event);
    }
}
