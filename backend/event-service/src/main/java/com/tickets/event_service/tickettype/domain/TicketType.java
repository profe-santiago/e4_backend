package com.tickets.event_service.tickettype.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Child entity del aggregate Event — representa un tipo de ticket.
 * POJO puro — sin Spring, sin JPA.
 *
 * Modelo rico: la lógica de reserva de stock vive aquí, no en el UseCase.
 */
public class TicketType {

    private Long id;
    private UUID eventId;
    private String name;
    private String description;
    private Money price;
    private int totalQuantity;
    private int availableQuantity;
    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;

    public TicketType() {}

    // ─── Factory method ───────────────────────────────────────────────────────

    public static TicketType create(UUID eventId, String name, String description,
                                     Money price, int totalQuantity,
                                     LocalDateTime saleStartDate, LocalDateTime saleEndDate) {
        TicketType tt = new TicketType();
        tt.eventId = eventId;
        tt.name = name;
        tt.description = description;
        tt.price = price;
        tt.totalQuantity = totalQuantity;
        tt.availableQuantity = totalQuantity;
        tt.saleStartDate = saleStartDate;
        tt.saleEndDate = saleEndDate;
        return tt;
    }

    // ─── Rich behavior ────────────────────────────────────────────────────────

    /**
     * Reserva stock para una orden.
     * Lanza excepción si el stock es insuficiente — el UseCase no toma esta decisión.
     */
    public void reserveStock(int quantity) {
        validateSalePeriod();
        if (availableQuantity < quantity) {
            throw new InsufficientStockException(name, availableQuantity, quantity);
        }
        this.availableQuantity -= quantity;
    }

    private void validateSalePeriod() {
        LocalDateTime now = LocalDateTime.now();
        if (saleStartDate != null && now.isBefore(saleStartDate)) {
            throw new SaleNotAvailableException(
                    "La venta de '" + name + "' aún no ha comenzado");
        }
        if (saleEndDate != null && now.isAfter(saleEndDate)) {
            throw new SaleNotAvailableException(
                    "La venta de '" + name + "' ha cerrado");
        }
    }

    /**
     * Libera stock reservado cuando una orden es cancelada o reembolsada.
     * No puede superar totalQuantity (garantía de integridad).
     */
    public void releaseStock(int quantity) {
        this.availableQuantity = Math.min(this.totalQuantity, this.availableQuantity + quantity);
    }

    /**
     * Actualiza los detalles del tipo de ticket.
     * Resetea el availableQuantity al nuevo totalQuantity.
     */
    public void updateDetails(String name, String description, Money price, int totalQuantity,
                               LocalDateTime saleStartDate, LocalDateTime saleEndDate) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
        this.saleStartDate = saleStartDate;
        this.saleEndDate = saleEndDate;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Money getPrice() { return price; }
    public int getTotalQuantity() { return totalQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
    public LocalDateTime getSaleStartDate() { return saleStartDate; }
    public LocalDateTime getSaleEndDate() { return saleEndDate; }

    // ─── Setters (solo para el mapper de persistencia) ───────────────────────

    public void setId(Long id) { this.id = id; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Money price) { this.price = price; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    public void setSaleStartDate(LocalDateTime saleStartDate) { this.saleStartDate = saleStartDate; }
    public void setSaleEndDate(LocalDateTime saleEndDate) { this.saleEndDate = saleEndDate; }
}
