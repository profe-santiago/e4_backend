package com.tickets.notification_service.notification.infrastructure.rest;

import com.tickets.notification_service.notification.application.GetUserNotificationsUseCase;
import com.tickets.notification_service.notification.application.dto.GetUserNotificationsQuery;
import com.tickets.notification_service.notification.application.dto.PagedResult;
import com.tickets.notification_service.notification.domain.Notification;
import com.tickets.notification_service.notification.domain.UserId;
import com.tickets.notification_service.notification.infrastructure.rest.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    public Map<String, Object> getByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        GetUserNotificationsQuery query = new GetUserNotificationsQuery(UserId.of(userId), page, size);
        PagedResult<Notification> result = useCase.execute(query);

        List<NotificationResponse> content = result.content().stream()
                .map(mapper::toResponse)
                .toList();

        return Map.of(
                "content", content,
                "totalElements", result.totalElements(),
                "totalPages", result.totalPages(),
                "number", result.page(),
                "size", result.size()
        );
    }
}
