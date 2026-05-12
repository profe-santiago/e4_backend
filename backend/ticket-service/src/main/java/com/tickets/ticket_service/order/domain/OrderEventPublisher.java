package com.tickets.ticket_service.order.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Puerto secundario (salida) para publicar eventos de orden.
 * Usa vocabulario de dominio — el adaptador (RabbitMQ) convierte a DTOs de mensajería.
 *
 * El dominio define QUÉ comunicar, no CÓMO (RabbitMQ, Kafka, etc.).
 */
public interface OrderEventPublisher {

    /**
     * Datos de un ítem para la reserva de stock.
     */
    record StockItem(UUID eventId, Long ticketTypeId, int quantity) {}

    /**
     * Datos de un ticket generado para publicar en OrderConfirmedEvent.
     */
    record TicketData(UUID ticketId, UUID eventId, Long ticketTypeId, String qrCode) {}

    /**
     * Datos de un ítem para la liberación de stock (cancelación o reembolso).
     */
    record StockReleaseItem(UUID eventId, Long ticketTypeId, int quantity) {}

    void publishStockReserve(UUID orderId, UUID userId, List<StockItem> items);

    void publishOrderConfirmed(UUID orderId, UUID userId, BigDecimal totalAmount,
                                String paymentIntentId, List<TicketData> tickets);

    void publishOrderCancelled(UUID orderId, UUID userId, String reason, List<StockReleaseItem> items);

    void publishRefundInitiated(UUID orderId, UUID userId);

    void publishOrderRefunded(UUID orderId, UUID userId, List<StockReleaseItem> items);
}
