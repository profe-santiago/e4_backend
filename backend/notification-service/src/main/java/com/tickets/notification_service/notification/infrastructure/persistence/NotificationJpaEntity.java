package com.tickets.notification_service.notification.infrastructure.persistence;

import com.tickets.notification_service.notification.domain.NotificationStatus;
import com.tickets.notification_service.notification.domain.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class NotificationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private NotificationType type;

    @Column(length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private NotificationStatus status;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
