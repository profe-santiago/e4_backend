package com.tickets.notification_service.notification;

import com.tickets.notification_service.notification.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Obtener historial de notificaciones de un usuario")
    public Page<NotificationResponse> getByUser(
            @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return notificationService.findByUserId(userId, pageable);
    }
}
