package com.tickets.ticket_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.OrderCancelledEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.OrderConfirmedEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.OrderRefundedEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.PaymentCompletedEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.PaymentFailedEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.RefundCompletedEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.RefundFailedEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.RefundInitiatedEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.StockFailedEvent;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.StockReserveCommand;
import com.tickets.ticket_service.order.infrastructure.messaging.dto.StockReservedEvent;
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
 * Topología RabbitMQ del ticket-service.
 *
 * Exchange principal: tickets.topic.exchange (TopicExchange)
 * DLX:               tickets.dlx (DirectExchange)
 *
 * Queues que PUBLICA ticket-service:
 *   stock.reserve.queue    ← routing key: stock.reserve   (event-service consume)
 *   order.events.queue     ← routing key: order.*         (futuro: payment/notification)
 *
 * Queues que CONSUME ticket-service:
 *   stock.reserved.queue   ← routing key: stock.reserved
 *   stock.failed.queue     ← routing key: stock.failed
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")        private String exchange;
    @Value("${app.rabbitmq.dlx}")             private String dlx;

    @Value("${app.rabbitmq.queues.stock-reserved}")  private String stockReservedQueue;
    @Value("${app.rabbitmq.queues.stock-failed}")    private String stockFailedQueue;
    @Value("${app.rabbitmq.queues.order-events}")    private String orderEventsQueue;
    @Value("${app.rabbitmq.queues.payment-failed}")   private String paymentFailedQueue;
    @Value("${app.rabbitmq.queues.refund-completed}") private String refundCompletedQueue;
    @Value("${app.rabbitmq.queues.refund-failed}")    private String refundFailedQueue;

    @Value("${app.rabbitmq.routing-keys.stock-reserve}")   private String rkStockReserve;
    @Value("${app.rabbitmq.routing-keys.stock-reserved}")  private String rkStockReserved;
    @Value("${app.rabbitmq.routing-keys.stock-failed}")    private String rkStockFailed;
    @Value("${app.rabbitmq.routing-keys.order-confirmed}") private String rkOrderConfirmed;
    @Value("${app.rabbitmq.routing-keys.order-cancelled}") private String rkOrderCancelled;
    @Value("${app.rabbitmq.routing-keys.payment-failed}")   private String rkPaymentFailed;
    @Value("${app.rabbitmq.routing-keys.refund-initiated}") private String rkRefundInitiated;
    @Value("${app.rabbitmq.routing-keys.refund-completed}") private String rkRefundCompleted;
    @Value("${app.rabbitmq.routing-keys.refund-failed}")    private String rkRefundFailed;
    @Value("${app.rabbitmq.routing-keys.order-refunded}")   private String rkOrderRefunded;

    // ── Exchanges ─────────────────────────────────────────────────────────────

    @Bean
    public TopicExchange ticketsExchange() {
        return ExchangeBuilder.topicExchange(exchange).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(dlx).durable(true).build();
    }

    // ── Queues consumidas por ticket-service ──────────────────────────────────

    @Bean
    public Queue stockReservedQueue() {
        return QueueBuilder.durable(stockReservedQueue)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", stockReservedQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue stockFailedQueue() {
        return QueueBuilder.durable(stockFailedQueue)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", stockFailedQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue orderEventsQueue() {
        return QueueBuilder.durable(orderEventsQueue).build();
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(paymentFailedQueue)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", paymentFailedQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue refundCompletedQueue() {
        return QueueBuilder.durable(refundCompletedQueue)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", refundCompletedQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue refundFailedQueue() {
        return QueueBuilder.durable(refundFailedQueue)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", refundFailedQueue + ".dlq")
                .build();
    }

    // ── Dead Letter Queues ────────────────────────────────────────────────────

    @Bean
    public Queue stockReservedDlq() {
        return QueueBuilder.durable(stockReservedQueue + ".dlq").build();
    }

    @Bean
    public Queue stockFailedDlq() {
        return QueueBuilder.durable(stockFailedQueue + ".dlq").build();
    }

    @Bean
    public Queue paymentFailedDlq() {
        return QueueBuilder.durable(paymentFailedQueue + ".dlq").build();
    }

    @Bean
    public Queue refundCompletedDlq() {
        return QueueBuilder.durable(refundCompletedQueue + ".dlq").build();
    }

    @Bean
    public Queue refundFailedDlq() {
        return QueueBuilder.durable(refundFailedQueue + ".dlq").build();
    }

    // ── Bindings ──────────────────────────────────────────────────────────────

    @Bean
    public Binding stockReservedBinding() {
        return BindingBuilder.bind(stockReservedQueue()).to(ticketsExchange()).with(rkStockReserved);
    }

    @Bean
    public Binding stockFailedBinding() {
        return BindingBuilder.bind(stockFailedQueue()).to(ticketsExchange()).with(rkStockFailed);
    }

    @Bean
    public Binding orderEventsBinding() {
        return BindingBuilder.bind(orderEventsQueue()).to(ticketsExchange()).with("order.*");
    }

    @Bean
    public Binding stockReservedDlqBinding() {
        return BindingBuilder.bind(stockReservedDlq()).to(deadLetterExchange())
                .with(stockReservedQueue + ".dlq");
    }

    @Bean
    public Binding stockFailedDlqBinding() {
        return BindingBuilder.bind(stockFailedDlq()).to(deadLetterExchange())
                .with(stockFailedQueue + ".dlq");
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder.bind(paymentFailedQueue()).to(ticketsExchange()).with(rkPaymentFailed);
    }

    @Bean
    public Binding paymentFailedDlqBinding() {
        return BindingBuilder.bind(paymentFailedDlq()).to(deadLetterExchange())
                .with(paymentFailedQueue + ".dlq");
    }

    @Bean
    public Binding refundCompletedBinding() {
        return BindingBuilder.bind(refundCompletedQueue()).to(ticketsExchange()).with(rkRefundCompleted);
    }

    @Bean
    public Binding refundCompletedDlqBinding() {
        return BindingBuilder.bind(refundCompletedDlq()).to(deadLetterExchange())
                .with(refundCompletedQueue + ".dlq");
    }

    @Bean
    public Binding refundFailedBinding() {
        return BindingBuilder.bind(refundFailedQueue()).to(ticketsExchange()).with(rkRefundFailed);
    }

    @Bean
    public Binding refundFailedDlqBinding() {
        return BindingBuilder.bind(refundFailedDlq()).to(deadLetterExchange())
                .with(refundFailedQueue + ".dlq");
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

    /**
     * Aliases de tipos para serialización cross-service.
     * Ambos servicios deben tener el mismo alias → el mapper local resuelve la clase correcta.
     * Así se evita usar FQCNs que rompen cuando los packages difieren.
     */
    private Map<String, Class<?>> typeIdMappings() {
        Map<String, Class<?>> mappings = new HashMap<>();
        // Aliases
        mappings.put("StockReserveCommand",    StockReserveCommand.class);
        mappings.put("StockReservedEvent",     StockReservedEvent.class);
        mappings.put("StockFailedEvent",       StockFailedEvent.class);
        mappings.put("OrderConfirmedEvent",    OrderConfirmedEvent.class);
        mappings.put("OrderCancelledEvent",    OrderCancelledEvent.class);
        mappings.put("OrderRefundedEvent",     OrderRefundedEvent.class);
        mappings.put("PaymentFailedEvent",     PaymentFailedEvent.class);
        mappings.put("PaymentCompletedEvent",  PaymentCompletedEvent.class);
        mappings.put("RefundInitiatedEvent",   RefundInitiatedEvent.class);
        mappings.put("RefundCompletedEvent",   RefundCompletedEvent.class);
        mappings.put("RefundFailedEvent",      RefundFailedEvent.class);
        // FQCNs de event-service (fallback cuando Spring AMQP envía FQCN en lugar de alias)
        mappings.put("com.tickets.event_service.tickettype.infrastructure.messaging.dto.StockReservedEvent", StockReservedEvent.class);
        mappings.put("com.tickets.event_service.tickettype.infrastructure.messaging.dto.StockFailedEvent",   StockFailedEvent.class);
        // FQCNs de payment-service
        mappings.put("com.tickets.payment_service.payment.infrastructure.messaging.event.PaymentCompletedEvent", PaymentCompletedEvent.class);
        mappings.put("com.tickets.payment_service.payment.infrastructure.messaging.event.PaymentFailedEvent",    PaymentFailedEvent.class);
        mappings.put("com.tickets.payment_service.payment.infrastructure.messaging.event.RefundCompletedEvent",  RefundCompletedEvent.class);
        mappings.put("com.tickets.payment_service.payment.infrastructure.messaging.event.RefundFailedEvent",    RefundFailedEvent.class);
        return mappings;
    }
}
