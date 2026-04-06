package com.tickets.notification_service.notification.dto;

import com.tickets.notification_service.notification.NotificationStatus;
import com.tickets.notification_service.notification.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private NotificationType type;
    private String subject;
    private String message;
    private NotificationStatus status;
    private UUID referenceId;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
