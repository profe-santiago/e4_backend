package com.tickets.ticket_service.messaging.publisher;

import com.tickets.ticket_service.messaging.event.OrderCancelledEvent;
import com.tickets.ticket_service.messaging.event.OrderConfirmedEvent;
import com.tickets.ticket_service.messaging.event.StockReserveCommand;

/**
 * Puerto de salida para publicar eventos de orden a RabbitMQ.
 * Desacopla el dominio del broker concreto — DIP aplicado.
 */
public interface OrderEventPublisher {
    void publishStockReserve(StockReserveCommand command);
    void publishOrderConfirmed(OrderConfirmedEvent event);
    void publishOrderCancelled(OrderCancelledEvent event);
}
