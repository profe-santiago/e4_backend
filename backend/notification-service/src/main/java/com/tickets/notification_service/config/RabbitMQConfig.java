package com.tickets.notification_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tickets.notification_service.notification.infrastructure.messaging.event.OrderCancelledEvent;
import com.tickets.notification_service.notification.infrastructure.messaging.event.OrderConfirmedEvent;
import com.tickets.notification_service.notification.infrastructure.messaging.event.PaymentCompletedEvent;
import com.tickets.notification_service.notification.infrastructure.messaging.event.RefundCompletedEvent;
import com.tickets.notification_service.notification.infrastructure.messaging.event.RefundFailedEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Topología RabbitMQ del notification-service.
 *
 * Exchange compartido: tickets.topic.exchange
 * DLX compartido:      tickets.dlx
 *
 * Queues que CONSUME notification-service:
 *   order.confirmed.notification.queue   ← routing key: order.confirmed
 *   payment.completed.notification.queue ← routing key: payment.completed
 *   order.cancelled.notification.queue  ← routing key: order.cancelled
 *
 * Cada queue tiene su DLQ correspondiente en tickets.dlx.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}") private String exchange;
    @Value("${app.rabbitmq.dlx}")      private String dlx;

    @Value("${app.rabbitmq.queues.order-confirmed}")   private String orderConfirmedQueue;
    @Value("${app.rabbitmq.queues.payment-completed}") private String paymentCompletedQueue;
    @Value("${app.rabbitmq.queues.order-cancelled}")   private String orderCancelledQueue;
    @Value("${app.rabbitmq.queues.refund-completed}")  private String refundCompletedQueue;
    @Value("${app.rabbitmq.queues.refund-failed}")     private String refundFailedQueue;

    @Value("${app.rabbitmq.routing-keys.order-confirmed}")   private String rkOrderConfirmed;
    @Value("${app.rabbitmq.routing-keys.payment-completed}") private String rkPaymentCompleted;
    @Value("${app.rabbitmq.routing-keys.order-cancelled}")   private String rkOrderCancelled;
    @Value("${app.rabbitmq.routing-keys.refund-completed}")  private String rkRefundCompleted;
    @Value("${app.rabbitmq.routing-keys.refund-failed}")     private String rkRefundFailed;

    // ── Exchanges ─────────────────────────────────────────────────────────────

    @Bean
    public TopicExchange ticketsExchange() {
        return ExchangeBuilder.topicExchange(exchange).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(dlx).durable(true).build();
    }

    // ── Queues ────────────────────────────────────────────────────────────────

    @Bean
    public Queue orderConfirmedNotificationQueue() {
        return withDlq(orderConfirmedQueue);
    }

    @Bean
    public Queue paymentCompletedNotificationQueue() {
        return withDlq(paymentCompletedQueue);
    }

    @Bean
    public Queue orderCancelledNotificationQueue() {
        return withDlq(orderCancelledQueue);
    }

    @Bean
    public Queue refundCompletedNotificationQueue() {
        return withDlq(refundCompletedQueue);
    }

    @Bean
    public Queue refundFailedNotificationQueue() {
        return withDlq(refundFailedQueue);
    }

    // ── Dead Letter Queues ────────────────────────────────────────────────────

    @Bean
    public Queue orderConfirmedNotificationDlq() {
        return QueueBuilder.durable(orderConfirmedQueue + ".dlq").build();
    }

    @Bean
    public Queue paymentCompletedNotificationDlq() {
        return QueueBuilder.durable(paymentCompletedQueue + ".dlq").build();
    }

    @Bean
    public Queue orderCancelledNotificationDlq() {
        return QueueBuilder.durable(orderCancelledQueue + ".dlq").build();
    }

    @Bean
    public Queue refundCompletedNotificationDlq() {
        return QueueBuilder.durable(refundCompletedQueue + ".dlq").build();
    }

    @Bean
    public Queue refundFailedNotificationDlq() {
        return QueueBuilder.durable(refundFailedQueue + ".dlq").build();
    }

    // ── Bindings ──────────────────────────────────────────────────────────────

    @Bean
    public Binding orderConfirmedBinding() {
        return BindingBuilder.bind(orderConfirmedNotificationQueue())
                .to(ticketsExchange()).with(rkOrderConfirmed);
    }

    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder.bind(paymentCompletedNotificationQueue())
                .to(ticketsExchange()).with(rkPaymentCompleted);
    }

    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder.bind(orderCancelledNotificationQueue())
                .to(ticketsExchange()).with(rkOrderCancelled);
    }

    @Bean
    public Binding orderConfirmedDlqBinding() {
        return BindingBuilder.bind(orderConfirmedNotificationDlq())
                .to(deadLetterExchange()).with(orderConfirmedQueue + ".dlq");
    }

    @Bean
    public Binding paymentCompletedDlqBinding() {
        return BindingBuilder.bind(paymentCompletedNotificationDlq())
                .to(deadLetterExchange()).with(paymentCompletedQueue + ".dlq");
    }

    @Bean
    public Binding orderCancelledDlqBinding() {
        return BindingBuilder.bind(orderCancelledNotificationDlq())
                .to(deadLetterExchange()).with(orderCancelledQueue + ".dlq");
    }

    @Bean
    public Binding refundCompletedBinding() {
        return BindingBuilder.bind(refundCompletedNotificationQueue())
                .to(ticketsExchange()).with(rkRefundCompleted);
    }

    @Bean
    public Binding refundCompletedDlqBinding() {
        return BindingBuilder.bind(refundCompletedNotificationDlq())
                .to(deadLetterExchange()).with(refundCompletedQueue + ".dlq");
    }

    @Bean
    public Binding refundFailedBinding() {
        return BindingBuilder.bind(refundFailedNotificationQueue())
                .to(ticketsExchange()).with(rkRefundFailed);
    }

    @Bean
    public Binding refundFailedDlqBinding() {
        return BindingBuilder.bind(refundFailedNotificationDlq())
                .to(deadLetterExchange()).with(refundFailedQueue + ".dlq");
    }

    // ── Serialización: Jackson con type aliases (cross-service safe) ──────────

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();

        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("com.tickets.*");
        classMapper.setIdClassMapping(typeIdMappings());

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(mapper);
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Los aliases deben coincidir exactamente con los usados en los servicios publicadores.
     * El mapper resuelve la clase local correcta sin depender del FQCN del servicio origen.
     */
    private Map<String, Class<?>> typeIdMappings() {
        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("OrderConfirmedEvent",   OrderConfirmedEvent.class);
        mappings.put("PaymentCompletedEvent", PaymentCompletedEvent.class);
        mappings.put("OrderCancelledEvent",   OrderCancelledEvent.class);
        mappings.put("RefundCompletedEvent",  RefundCompletedEvent.class);
        mappings.put("RefundFailedEvent",     RefundFailedEvent.class);
        return mappings;
    }

    private Queue withDlq(String queueName) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", queueName + ".dlq")
                .build();
    }
}
