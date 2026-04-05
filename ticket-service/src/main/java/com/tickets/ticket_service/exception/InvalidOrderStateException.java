package com.tickets.ticket_service.exception;

import com.tickets.ticket_service.order.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(OrderStatus from, OrderStatus to) {
        super("Transición de estado inválida para la orden: " + from + " → " + to);
    }
}
