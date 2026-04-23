package com.tickets.notification_service.notification.infrastructure.rest;

import com.tickets.notification_service.notification.domain.Notification;
import com.tickets.notification_service.notification.infrastructure.rest.dto.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
class NotificationRestMapper {

    NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId().value())
                .userId(notification.getUserId().value())
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
