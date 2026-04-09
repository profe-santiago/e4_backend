package com.tickets.event_service.category.domain;

/**
 * Aggregate root del bounded context Category.
 * POJO puro — sin Spring, sin JPA, sin ninguna librería de infraestructura.
 */
public class Category {

    private Long id;
    private String name;
    private String description;

    public Category() {}

    public Category(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // ─── Rich behavior ────────────────────────────────────────────────────────

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }
        this.name = newName.trim();
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    // ─── Setters (solo para el mapper de persistencia) ───────────────────────

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
}
