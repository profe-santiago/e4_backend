package com.tickets.notification_service.notification.domain.port;

import com.tickets.notification_service.notification.domain.Notification;
import com.tickets.notification_service.notification.domain.NotificationType;
import com.tickets.notification_service.notification.domain.UserId;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);

    List<Notification> findByUserId(UserId userId, int offset, int limit);

    long countByUserId(UserId userId);

    boolean existsByReferenceIdAndType(UUID referenceId, NotificationType type);
}
