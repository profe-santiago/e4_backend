package com.tickets.auth_service.credential.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio Credential. POJO puro — sin Spring, sin JPA.
 * Contiene las credenciales de autenticación de un usuario.
 */
public class Credential {

    private Long id;
    private UUID userId;
    private String email;
    private String passwordHash;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;

    public Credential() {}

    // ─── Factory method ───────────────────────────────────────────────────────

    public static Credential create(String email, String passwordHash) {
        Credential c = new Credential();
        c.userId = UUID.randomUUID();
        c.email = email;
        c.passwordHash = passwordHash;
        c.role = "BUYER";
        c.active = true;
        c.createdAt = LocalDateTime.now();
        return c;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ─── Setters (solo para el mapper de persistencia) ────────────────────────

    public void setId(Long id) { this.id = id; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(String role) { this.role = role; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
