package com.tickets.event_service.tickettype.domain;

import java.util.List;
import java.util.UUID;

/**
 * Puerto secundario (salida) para publicar resultados de reserva de stock.
 * Interface en el dominio — implementada en infrastructure/messaging (RabbitMQ).
 *
 * El dominio define QUÉ publicar, no CÓMO.
 */
public interface StockEventPublisher {

    void publishReserved(UUID orderId, List<ReservedStockItem> reservedItems);

    void publishFailed(UUID orderId, String reason);
}
