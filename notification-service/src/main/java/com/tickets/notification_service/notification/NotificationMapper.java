package com.tickets.notification_service.notification;

import com.tickets.notification_service.notification.dto.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .referenceId(notification.getReferenceId())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
