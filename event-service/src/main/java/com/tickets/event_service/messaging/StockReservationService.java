package com.tickets.event_service.messaging;

import com.tickets.event_service.messaging.event.StockReserveCommand;

/**
 * Puerto de entrada para reserva de stock desde mensajería.
 * Desacopla el consumer de RabbitMQ de la lógica de negocio.
 */
public interface StockReservationService {
    void reserve(StockReserveCommand command);
}
