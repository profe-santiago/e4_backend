package com.tickets.payment_service.messaging.consumer;

import com.tickets.payment_service.messaging.event.OrderConfirmedEvent;
import com.tickets.payment_service.payment.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Escucha órdenes confirmadas desde ticket-service.
 * Cuando llega → delega al PaymentService para procesar el cobro con Stripe.
 */
@Component
public class OrderConfirmedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmedConsumer.class);

    private final PaymentService paymentService;

    public OrderConfirmedConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.order-confirmed}")
    public void handle(OrderConfirmedEvent event) {
        log.info("[CONSUME] order.confirmed → orderId={}, userId={}, amount={}",
                event.getOrderId(), event.getUserId(), event.getTotalAmount());
        paymentService.processPayment(event);
    }
}
