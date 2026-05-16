package com.tickets.notification_service.notification.application;

import com.tickets.notification_service.notification.application.dto.SendOrderCancelledCommand;
import com.tickets.notification_service.notification.domain.Notification;
import com.tickets.notification_service.notification.domain.NotificationType;
import com.tickets.notification_service.notification.domain.UserId;
import com.tickets.notification_service.notification.domain.port.NotificationEmailPort;
import com.tickets.notification_service.notification.domain.port.NotificationRepository;
import com.tickets.notification_service.notification.domain.port.UserGateway;
import com.tickets.notification_service.shared.annotation.UseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UseCase
public class SendOrderCancelledNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendOrderCancelledNotificationUseCase.class);

    private final NotificationRepository repository;
    private final UserGateway userGateway;
    private final NotificationEmailPort emailPort;

    public SendOrderCancelledNotificationUseCase(NotificationRepository repository,
                                                 UserGateway userGateway,
                                                 NotificationEmailPort emailPort) {
        this.repository = repository;
        this.userGateway = userGateway;
        this.emailPort = emailPort;
    }

    public void execute(SendOrderCancelledCommand command) {
        log.info("[UC] SendOrderCancelled → orderId={}, userId={}", command.orderId(), command.userId());

        if (repository.existsByReferenceIdAndType(command.orderId(), NotificationType.EVENT_CANCELLED)) {
            log.warn("[UC] Notificación duplicada ignorada → orderId={}, type=EVENT_CANCELLED", command.orderId());
            return;
        }

        UserId userId = UserId.of(command.userId());
        String reason = command.reason() != null ? command.reason() : "Error en el procesamiento";

        String subject = "Tu orden fue cancelada - #" + command.orderId();
        String message = "Tu orden #" + command.orderId() + " fue cancelada. Motivo: " + reason;

        Notification notification = Notification.create(
                userId,
                NotificationType.EVENT_CANCELLED,
                subject,
                message,
                command.orderId()
        );

        userGateway.findById(userId).ifPresentOrElse(
                user -> {
                    try {
                        emailPort.sendOrderCancelled(user.email(), user.firstName(), command.orderId(), reason);
                        notification.markAsSent();
                    } catch (Exception e) {
                        log.error("[UC] Fallo al enviar email de orden cancelada: userId={}, error={}",
                                command.userId(), e.getMessage());
                        notification.markAsFailed();
                    }
                },
                () -> {
                    log.warn("[UC] Usuario no encontrado, notificación marcada como FAILED: userId={}", command.userId());
                    notification.markAsFailed();
                }
        );

        repository.save(notification);
    }
}
