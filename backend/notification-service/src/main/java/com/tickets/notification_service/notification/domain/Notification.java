package com.tickets.notification_service.notification.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Aggregate root del dominio de notificaciones.
 * POJO puro — sin dependencias de Spring, JPA ni ninguna librería de infraestructura.
 *
 * La lógica de transición de estado vive aquí, no en los use cases.
 */
public class Notification {

    private NotificationId id;
    private UserId userId;
    private NotificationType type;
    private String subject;
    private String message;
    private NotificationStatus status;
    private UUID referenceId;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    private Notification() {
    }

    // ── Factory: crear nueva notificación ────────────────────────────────────

    public static Notification create(UserId userId,
                                      NotificationType type,
                                      String subject,
                                      String message,
                                      UUID referenceId) {
        Notification n = new Notification();
        n.id = NotificationId.generate();
        n.userId = userId;
        n.type = type;
        n.subject = subject;
        n.message = message;
        n.referenceId = referenceId;
        n.status = NotificationStatus.PENDING;
        n.createdAt = LocalDateTime.now();
        return n;
    }

    // ── Factory: reconstituir desde persistencia ──────────────────────────────

    public static Notification reconstitute(NotificationId id,
                                            UserId userId,
                                            NotificationType type,
                                            String subject,
                                            String message,
                                            NotificationStatus status,
                                            UUID referenceId,
                                            LocalDateTime sentAt,
                                            LocalDateTime createdAt) {
        Notification n = new Notification();
        n.id = id;
        n.userId = userId;
        n.type = type;
        n.subject = subject;
        n.message = message;
        n.status = status;
        n.referenceId = referenceId;
        n.sentAt = sentAt;
        n.createdAt = createdAt;
        return n;
    }

    // ── Comportamiento de dominio ─────────────────────────────────────────────

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
    }

    // ── Getters (sin setters: las transiciones pasan por métodos de dominio) ──

    public NotificationId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
