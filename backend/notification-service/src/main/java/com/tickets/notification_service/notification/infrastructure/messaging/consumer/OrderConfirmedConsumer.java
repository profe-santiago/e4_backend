package com.tickets.notification_service.notification.infrastructure.messaging.consumer;

import com.tickets.notification_service.notification.application.SendOrderConfirmedNotificationUseCase;
import com.tickets.notification_service.notification.application.dto.SendOrderConfirmedCommand;
import com.tickets.notification_service.notification.infrastructure.messaging.event.OrderConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderConfirmedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmedConsumer.class);

    private final SendOrderConfirmedNotificationUseCase useCase;

    public OrderConfirmedConsumer(SendOrderConfirmedNotificationUseCase useCase) {
        this.useCase = useCase;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.order-confirmed}")
    public void handle(OrderConfirmedEvent event) {
        log.info("[CONSUME] order.confirmed → orderId={}, userId={}", event.getOrderId(), event.getUserId());

        List<SendOrderConfirmedCommand.ConfirmedTicket> tickets = event.getTickets() == null ? List.of() :
                event.getTickets().stream()
                        .map(t -> new SendOrderConfirmedCommand.ConfirmedTicket(
                                t.getTicketId(), t.getEventId(), t.getTicketTypeId(), t.getQrCode()))
                        .toList();

        useCase.execute(new SendOrderConfirmedCommand(
                event.getOrderId(),
                event.getUserId(),
                event.getTotalAmount(),
                tickets
        ));
    }
}
