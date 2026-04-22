package com.tickets.event_service.event.domain;

import com.tickets.event_service.category.domain.Category;
import com.tickets.event_service.exception.InvalidEventStatusTransitionException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Aggregate root del bounded context Event.
 * POJO puro — sin Spring, sin JPA, sin ninguna librería de infraestructura.
 *
 * Modelo rico: la lógica de negocio vive aquí, no en el UseCase/Service.
 */
public class Event {

    private UUID id;
    private UUID organizerId;
    private String title;
    private String description;
    private Category category;   // domain Category — sin @ManyToOne, sin JPA
    private String venue;
    private String city;
    private String country;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String imageUrl;
    private EventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Event() {}

    // ─── Factory method ───────────────────────────────────────────────────────

    public static Event create(UUID organizerId, String title, String description,
                                Category category, String venue, String city, String country,
                                LocalDateTime startDate, LocalDateTime endDate, String imageUrl) {
        Event event = new Event();
        event.organizerId = organizerId;
        event.title = title;
        event.description = description;
        event.category = category;
        event.venue = venue;
        event.city = city;
        event.country = country;
        event.startDate = startDate;
        event.endDate = endDate;
        event.imageUrl = imageUrl;
        event.status = EventStatus.DRAFT;
        event.createdAt = LocalDateTime.now();
        return event;
    }

    // ─── Rich behavior ────────────────────────────────────────────────────────

    /**
     * Cambia el estado del evento validando la máquina de estados.
     * Es el ÚNICO punto de entrada para transiciones de estado.
     */
    public void changeStatus(EventStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidEventStatusTransitionException(status, newStatus);
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Actualiza los campos del evento — solo aplica los valores no nulos.
     */
    public void update(String title, String description, Category category,
                       String venue, String city, String country,
                       LocalDateTime startDate, LocalDateTime endDate, String imageUrl) {
        if (title != null)       this.title = title;
        if (description != null) this.description = description;
        if (category != null)    this.category = category;
        if (venue != null)       this.venue = venue;
        if (city != null)        this.city = city;
        if (country != null)     this.country = country;
        if (startDate != null)   this.startDate = startDate;
        if (endDate != null)     this.endDate = endDate;
        if (imageUrl != null)    this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public UUID getOrganizerId() { return organizerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public String getVenue() { return venue; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public String getImageUrl() { return imageUrl; }
    public EventStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ─── Setters (solo para el mapper de persistencia) ───────────────────────

    public void setId(UUID id) { this.id = id; }
    public void setOrganizerId(UUID organizerId) { this.organizerId = organizerId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(Category category) { this.category = category; }
    public void setVenue(String venue) { this.venue = venue; }
    public void setCity(String city) { this.city = city; }
    public void setCountry(String country) { this.country = country; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setStatus(EventStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
