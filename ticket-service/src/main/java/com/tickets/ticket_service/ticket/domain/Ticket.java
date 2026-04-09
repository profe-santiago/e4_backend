package com.tickets.ticket_service.ticket.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio del bounded context Ticket.
 * POJO puro — sin Spring, sin JPA.
 *
 * Referencia a OrderItem por ID (no por objeto completo) para respetar boundaries.
 * El campo orderId está denormalizado desde el JPA join — evita cruzar agregados en consultas.
 */
public class Ticket {

    private UUID id;
    private Long orderItemId;
    private UUID orderId;       // denormalizado desde JPA join (OrderItem → Order)
    private UUID userId;
    private UUID eventId;
    private Long ticketTypeId;
    private String qrCode;
    private TicketStatus status;
    private LocalDateTime purchasedAt;
    private LocalDateTime usedAt;

    public Ticket() {}

    // ─── Factory method ───────────────────────────────────────────────────

    public static Ticket create(Long orderItemId, UUID userId, UUID eventId,
                                 Long ticketTypeId, String qrCode) {
        Ticket ticket = new Ticket();
        ticket.orderItemId = orderItemId;
        ticket.userId = userId;
        ticket.eventId = eventId;
        ticket.ticketTypeId = ticketTypeId;
        ticket.qrCode = qrCode;
        ticket.status = TicketStatus.ACTIVE;
        ticket.purchasedAt = LocalDateTime.now();
        return ticket;
    }

    // ─── Rich behavior ────────────────────────────────────────────────────

    public void markUsed() {
        this.status = TicketStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = TicketStatus.CANCELLED;
    }

    // ─── Getters ─────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public Long getOrderItemId() { return orderItemId; }
    public UUID getOrderId() { return orderId; }
    public UUID getUserId() { return userId; }
    public UUID getEventId() { return eventId; }
    public Long getTicketTypeId() { return ticketTypeId; }
    public String getQrCode() { return qrCode; }
    public TicketStatus getStatus() { return status; }
    public LocalDateTime getPurchasedAt() { return purchasedAt; }
    public LocalDateTime getUsedAt() { return usedAt; }

    // ─── Setters (solo para el mapper de persistencia) ───────────────────

    public void setId(UUID id) { this.id = id; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public void setTicketTypeId(Long ticketTypeId) { this.ticketTypeId = ticketTypeId; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public void setPurchasedAt(LocalDateTime purchasedAt) { this.purchasedAt = purchasedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
