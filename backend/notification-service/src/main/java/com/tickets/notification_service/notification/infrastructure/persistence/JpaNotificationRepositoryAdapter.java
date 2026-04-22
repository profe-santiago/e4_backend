package com.tickets.notification_service.notification.infrastructure.persistence;

import com.tickets.notification_service.notification.domain.Notification;
import com.tickets.notification_service.notification.domain.UserId;
import com.tickets.notification_service.notification.domain.port.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class JpaNotificationRepositoryAdapter implements NotificationRepository {

    private final JpaNotificationRepository jpaRepository;
    private final NotificationPersistenceMapper mapper;

    JpaNotificationRepositoryAdapter(JpaNotificationRepository jpaRepository,
                                     NotificationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(notification));
        return mapper.toDomain(saved);
    }

    @Override
    public List<Notification> findByUserId(UserId userId, int offset, int limit) {
        int page = limit == 0 ? 0 : offset / limit;
        PageRequest pageable = PageRequest.of(page, limit == 0 ? 20 : limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId.value(), pageable)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByUserId(UserId userId) {
        return jpaRepository.countByUserId(userId.value());
    }
}
