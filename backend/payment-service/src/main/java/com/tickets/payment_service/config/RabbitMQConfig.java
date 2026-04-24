package com.tickets.payment_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tickets.payment_service.payment.infrastructure.messaging.event.OrderConfirmedEvent;
import com.tickets.payment_service.payment.infrastructure.messaging.event.PaymentCompletedEvent;
import com.tickets.payment_service.payment.infrastructure.messaging.event.PaymentFailedEvent;
import com.tickets.payment_service.payment.infrastructure.messaging.event.RefundCompletedEvent;
import com.tickets.payment_service.payment.infrastructure.messaging.event.RefundFailedEvent;
import com.tickets.payment_service.payment.infrastructure.messaging.event.RefundInitiatedEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
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
 * Topología RabbitMQ del payment-service.
 *
 * Exchange: tickets.topic.exchange (compartido con todos los servicios)
 * DLX:      tickets.dlx
 *
 * Queues que CONSUME payment-service:
 *   order.confirmed.payment.queue  ← routing key: order.confirmed
 *
 * Queues que PUBLICA payment-service:
 *   → routing key: payment.completed  (ticket-service / notification-service)
 *   → routing key: payment.failed     (ticket-service)
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}") private String exchange;
    @Value("${app.rabbitmq.dlx}")      private String dlx;

    @Value("${app.rabbitmq.queues.order-confirmed}")  private String orderConfirmedQueue;
    @Value("${app.rabbitmq.queues.refund-initiated}") private String refundInitiatedQueue;

    @Value("${app.rabbitmq.routing-keys.order-confirmed}")   private String rkOrderConfirmed;
    @Value("${app.rabbitmq.routing-keys.refund-initiated}")  private String rkRefundInitiated;
    @Value("${app.rabbitmq.routing-keys.refund-completed}")  private String rkRefundCompleted;

    // ── Exchanges ─────────────────────────────────────────────────────────────

    @Bean
    public TopicExchange ticketsExchange() {
        return ExchangeBuilder.topicExchange(exchange).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(dlx).durable(true).build();
    }

    // ── Queues consumidas por payment-service ─────────────────────────────────

    @Bean
    public Queue orderConfirmedQueue() {
        return QueueBuilder.durable(orderConfirmedQueue)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", orderConfirmedQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue refundInitiatedQueue() {
        return QueueBuilder.durable(refundInitiatedQueue)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", refundInitiatedQueue + ".dlq")
                .build();
    }

    // ── Dead Letter Queues ────────────────────────────────────────────────────

    @Bean
    public Queue orderConfirmedDlq() {
        return QueueBuilder.durable(orderConfirmedQueue + ".dlq").build();
    }

    @Bean
    public Queue refundInitiatedDlq() {
        return QueueBuilder.durable(refundInitiatedQueue + ".dlq").build();
    }

    // ── Bindings ──────────────────────────────────────────────────────────────

    @Bean
    public Binding orderConfirmedBinding() {
        return BindingBuilder.bind(orderConfirmedQueue()).to(ticketsExchange()).with(rkOrderConfirmed);
    }

    @Bean
    public Binding orderConfirmedDlqBinding() {
        return BindingBuilder.bind(orderConfirmedDlq()).to(deadLetterExchange())
                .with(orderConfirmedQueue + ".dlq");
    }

    @Bean
    public Binding refundInitiatedBinding() {
        return BindingBuilder.bind(refundInitiatedQueue()).to(ticketsExchange()).with(rkRefundInitiated);
    }

    @Bean
    public Binding refundInitiatedDlqBinding() {
        return BindingBuilder.bind(refundInitiatedDlq()).to(deadLetterExchange())
                .with(refundInitiatedQueue + ".dlq");
    }

    // ── Serialización: Jackson con type aliases (cross-service safe) ──────────

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();

        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*");
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

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    /**
     * Los aliases deben coincidir exactamente con los usados en ticket-service.
     * El mapper resuelve la clase local correcta sin depender del FQCN.
     */
    private Map<String, Class<?>> typeIdMappings() {
        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("OrderConfirmedEvent",   OrderConfirmedEvent.class);
        mappings.put("PaymentCompletedEvent", PaymentCompletedEvent.class);
        mappings.put("PaymentFailedEvent",    PaymentFailedEvent.class);
        mappings.put("RefundInitiatedEvent",  RefundInitiatedEvent.class);
        mappings.put("RefundCompletedEvent",  RefundCompletedEvent.class);
        mappings.put("RefundFailedEvent",     RefundFailedEvent.class);
        // FQCNs de ticket-service (fallback cuando Spring AMQP envía FQCN en lugar de alias)
        mappings.put("com.tickets.ticket_service.order.infrastructure.messaging.dto.OrderConfirmedEvent", OrderConfirmedEvent.class);
        mappings.put("com.tickets.ticket_service.order.infrastructure.messaging.dto.RefundInitiatedEvent", RefundInitiatedEvent.class);
        return mappings;
    }
}
