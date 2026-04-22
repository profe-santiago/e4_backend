package com.tickets.notification_service.notification.infrastructure.rest;

import com.tickets.notification_service.notification.application.GetUserNotificationsUseCase;
import com.tickets.notification_service.notification.application.dto.GetUserNotificationsQuery;
import com.tickets.notification_service.notification.application.dto.PagedResult;
import com.tickets.notification_service.notification.domain.Notification;
import com.tickets.notification_service.notification.domain.UserId;
import com.tickets.notification_service.notification.infrastructure.rest.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications")
public class NotificationController {

    private final GetUserNotificationsUseCase useCase;
    private final NotificationRestMapper mapper;

    public NotificationController(GetUserNotificationsUseCase useCase, NotificationRestMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Obtener historial de notificaciones de un usuario")
    public Page<NotificationResponse> getByUser(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        GetUserNotificationsQuery query = new GetUserNotificationsQuery(
                UserId.of(userId),
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        PagedResult<Notification> result = useCase.execute(query);

        List<NotificationResponse> responses = result.content().stream()
                .map(mapper::toResponse)
                .toList();

        return new PageImpl<>(responses, PageRequest.of(result.page(), result.size()), result.totalElements());
    }
}
