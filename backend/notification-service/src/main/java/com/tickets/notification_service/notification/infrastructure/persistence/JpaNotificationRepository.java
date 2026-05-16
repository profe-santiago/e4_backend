package com.tickets.notification_service.notification.infrastructure.persistence;

import com.tickets.notification_service.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface JpaNotificationRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, org.springframework.data.domain.Pageable pageable);

    long countByUserId(UUID userId);

    boolean existsByReferenceIdAndType(UUID referenceId, NotificationType type);
}
