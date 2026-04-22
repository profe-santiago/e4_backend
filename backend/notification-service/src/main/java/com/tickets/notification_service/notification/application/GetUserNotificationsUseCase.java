package com.tickets.notification_service.notification.application;

import com.tickets.notification_service.notification.application.dto.GetUserNotificationsQuery;
import com.tickets.notification_service.notification.application.dto.PagedResult;
import com.tickets.notification_service.notification.domain.Notification;
import com.tickets.notification_service.notification.domain.port.NotificationRepository;
import com.tickets.notification_service.shared.annotation.UseCase;

import java.util.List;

@UseCase
public class GetUserNotificationsUseCase {

    private final NotificationRepository repository;

    public GetUserNotificationsUseCase(NotificationRepository repository) {
        this.repository = repository;
    }

    public PagedResult<Notification> execute(GetUserNotificationsQuery query) {
        int offset = query.page() * query.size();
        List<Notification> content = repository.findByUserId(query.userId(), offset, query.size());
        long total = repository.countByUserId(query.userId());
        return new PagedResult<>(content, total, query.page(), query.size());
    }
}
