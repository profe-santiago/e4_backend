package com.tickets.notification_service.notification.infrastructure.persistence;

import com.tickets.notification_service.notification.domain.Notification;
import com.tickets.notification_service.notification.domain.NotificationId;
import com.tickets.notification_service.notification.domain.UserId;
import org.springframework.stereotype.Component;

@Component
class NotificationPersistenceMapper {

    NotificationJpaEntity toJpaEntity(Notification domain) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(domain.getId().value());
        entity.setUserId(domain.getUserId().value());
        entity.setType(domain.getType());
        entity.setSubject(domain.getSubject());
        entity.setMessage(domain.getMessage());
        entity.setStatus(domain.getStatus());
        entity.setReferenceId(domain.getReferenceId());
        entity.setSentAt(domain.getSentAt());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    Notification toDomain(NotificationJpaEntity entity) {
        return Notification.reconstitute(
                NotificationId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                entity.getType(),
                entity.getSubject(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getReferenceId(),
                entity.getSentAt(),
                entity.getCreatedAt()
        );
    }
}
