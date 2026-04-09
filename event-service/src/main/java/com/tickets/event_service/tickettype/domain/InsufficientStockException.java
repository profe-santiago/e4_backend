package com.tickets.event_service.tickettype.domain;

/**
 * Excepción de dominio — lanzada cuando no hay suficiente stock disponible.
 * Sin dependencias de Spring o infraestructura.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String ticketTypeName, int available, int requested) {
        super(String.format(
                "Stock insuficiente para '%s': disponible=%d, solicitado=%d",
                ticketTypeName, available, requested
        ));
    }
}
