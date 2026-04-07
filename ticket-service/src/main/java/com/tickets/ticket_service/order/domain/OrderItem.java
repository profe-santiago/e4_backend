package com.tickets.ticket_service.order.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Child entity del aggregate Order — representa un ítem de la orden.
 * POJO puro — sin Spring, sin JPA.
 */
public class OrderItem {

    private Long id;
    private UUID eventId;
    private Long ticketTypeId;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderItem() {}

    public static OrderItem create(UUID eventId, Long ticketTypeId, int quantity) {
        OrderItem item = new OrderItem();
        item.eventId = eventId;
        item.ticketTypeId = ticketTypeId;
        item.quantity = quantity;
        item.unitPrice = BigDecimal.ZERO; // se actualiza al confirmar el stock
        return item;
    }

    // ─── Rich behavior ─────────────────────────────────────────────────────

    public void applyPrice(BigDecimal price) {
        this.unitPrice = price;
    }

    public boolean matchesTicketType(UUID eventId, Long ticketTypeId) {
        return this.eventId.equals(eventId) && this.ticketTypeId.equals(ticketTypeId);
    }

    // ─── Getters ──────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public UUID getEventId() { return eventId; }
    public Long getTicketTypeId() { return ticketTypeId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }

    // ─── Setters (solo para el mapper de persistencia) ────────────────────

    public void setId(Long id) { this.id = id; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public void setTicketTypeId(Long ticketTypeId) { this.ticketTypeId = ticketTypeId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
