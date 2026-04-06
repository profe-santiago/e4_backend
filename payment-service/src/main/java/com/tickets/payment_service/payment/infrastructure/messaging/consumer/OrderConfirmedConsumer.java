package com.tickets.payment_service.payment.infrastructure.messaging.consumer;

import com.tickets.payment_service.payment.application.ProcessPaymentUseCase;
import com.tickets.payment_service.payment.application.dto.ProcessPaymentCommand;
import com.tickets.payment_service.payment.infrastructure.messaging.event.OrderConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada para mensajes RabbitMQ.
 *
 * Actúa como anti-corruption layer: recibe el OrderConfirmedEvent del
 * ticket-service, extrae sólo los campos relevantes para el pago y
 * construye un ProcessPaymentCommand tipado para el use case.
 *
 * La currency por defecto es "MXN" dado que es el contexto del negocio.
 * Si eventualmente se soporte multi-moneda, el evento debería incluir la moneda.
 */
@Component
public class OrderConfirmedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmedConsumer.class);
    private static final String DEFAULT_CURRENCY = "MXN";

    private final ProcessPaymentUseCase processPaymentUseCase;

    public OrderConfirmedConsumer(ProcessPaymentUseCase processPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.order-confirmed}")
    public void handle(OrderConfirmedEvent event) {
        log.info("[RMQ] Received OrderConfirmedEvent: orderId={}, userId={}",
                event.getOrderId(), event.getUserId());

        ProcessPaymentCommand command = new ProcessPaymentCommand(
                event.getOrderId(),
                event.getUserId(),
                event.getTotalAmount(),
                DEFAULT_CURRENCY,
                event.getPaymentMethodId()
        );

        processPaymentUseCase.execute(command);
    }
}
