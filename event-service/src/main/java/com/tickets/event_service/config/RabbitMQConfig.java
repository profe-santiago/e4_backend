package com.tickets.event_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tickets.event_service.tickettype.infrastructure.messaging.dto.StockFailedEvent;
import com.tickets.event_service.tickettype.infrastructure.messaging.dto.StockReserveCommand;
import com.tickets.event_service.tickettype.infrastructure.messaging.dto.StockReservedEvent;
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
 * Configuración de RabbitMQ.
 * La topología DEBE coincidir con la de ticket-service.
 * Ambos servicios declaran las mismas colas/exchanges — RabbitMQ es idempotente.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")               private String exchange;
    @Value("${app.rabbitmq.dlx}")                    private String dlx;
    @Value("${app.rabbitmq.queues.stock-reserve}")   private String stockReserveQueue;
    @Value("${app.rabbitmq.routing-keys.stock-reserve}")  private String rkStockReserve;
    @Value("${app.rabbitmq.routing-keys.stock-reserved}") private String rkStockReserved;
    @Value("${app.rabbitmq.routing-keys.stock-failed}")   private String rkStockFailed;

    @Bean
    public TopicExchange ticketsExchange() {
        return ExchangeBuilder.topicExchange(exchange).durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(dlx).durable(true).build();
    }

    @Bean
    public Queue stockReserveQueue() {
        return QueueBuilder.durable(stockReserveQueue)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", stockReserveQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue stockReserveDlq() {
        return QueueBuilder.durable(stockReserveQueue + ".dlq").build();
    }

    @Bean
    public Binding stockReserveBinding() {
        return BindingBuilder.bind(stockReserveQueue()).to(ticketsExchange()).with(rkStockReserve);
    }

    @Bean
    public Binding stockReserveDlqBinding() {
        return BindingBuilder.bind(stockReserveDlq()).to(deadLetterExchange())
                .with(stockReserveQueue + ".dlq");
    }

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

    private Map<String, Class<?>> typeIdMappings() {
        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("StockReserveCommand", StockReserveCommand.class);
        mappings.put("StockReservedEvent",  StockReservedEvent.class);
        mappings.put("StockFailedEvent",    StockFailedEvent.class);
        return mappings;
    }
}
