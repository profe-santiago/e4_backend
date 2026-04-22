package com.tickets.user_service.user.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio User. POJO puro — sin Spring, sin JPA.
 * Modelo rico: la lógica de actualización parcial vive aquí, no en el UseCase.
 */
public class User {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    // ─── Factory method ───────────────────────────────────────────────────────

    public static User create(UUID id, String firstName, String lastName, String email,
                               String phone, LocalDate birthDate, String avatarUrl) {
        User user = new User();
        user.id = id;
        user.firstName = firstName;
        user.lastName = lastName;
        user.email = email;
        user.phone = phone;
        user.birthDate = birthDate;
        user.avatarUrl = avatarUrl;
        user.createdAt = LocalDateTime.now();
        return user;
    }

    // ─── Rich behavior ────────────────────────────────────────────────────────

    /**
     * Actualización parcial: solo sobreescribe los campos que lleguen no nulos.
     */
    public void update(String firstName, String lastName, String phone,
                        LocalDate birthDate, String avatarUrl) {
        if (firstName != null)  this.firstName = firstName;
        if (lastName != null)   this.lastName = lastName;
        if (phone != null)      this.phone = phone;
        if (birthDate != null)  this.birthDate = birthDate;
        if (avatarUrl != null)  this.avatarUrl = avatarUrl;
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getAvatarUrl() { return avatarUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ─── Setters (solo para el mapper de persistencia) ────────────────────────

    public void setId(UUID id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
