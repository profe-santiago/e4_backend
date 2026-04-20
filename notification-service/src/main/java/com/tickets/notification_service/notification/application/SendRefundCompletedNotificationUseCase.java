package com.tickets.notification_service.notification.application;

import com.tickets.notification_service.notification.application.dto.SendRefundCompletedCommand;
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
public class SendRefundCompletedNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendRefundCompletedNotificationUseCase.class);

    private final NotificationRepository repository;
    private final UserGateway userGateway;
    private final NotificationEmailPort emailPort;

    public SendRefundCompletedNotificationUseCase(NotificationRepository repository,
                                                  UserGateway userGateway,
                                                  NotificationEmailPort emailPort) {
        this.repository = repository;
        this.userGateway = userGateway;
        this.emailPort = emailPort;
    }

    public void execute(SendRefundCompletedCommand command) {
        log.info("[UC] SendRefundCompleted → orderId={}, userId={}", command.orderId(), command.userId());

        UserId userId = UserId.of(command.userId());

        String subject = "Tu reembolso fue procesado - Orden #" + command.orderId();
        String message = "El reembolso de tu orden #" + command.orderId() + " fue procesado exitosamente.";

        Notification notification = Notification.create(
                userId,
                NotificationType.REFUND_COMPLETED,
                subject,
                message,
                command.orderId()
        );

        userGateway.findById(userId).ifPresentOrElse(
                user -> {
                    try {
                        emailPort.sendRefundCompleted(user.email(), user.firstName(), command.orderId());
                        notification.markAsSent();
                    } catch (Exception e) {
                        log.error("[UC] Fallo al enviar email de reembolso completado: userId={}, error={}",
                                command.userId(), e.getMessage());
                        notification.markAsFailed();
                    }
                },
                () -> {
                    log.warn("[UC] Usuario no encontrado: userId={}", command.userId());
                    notification.markAsFailed();
                }
        );

        repository.save(notification);
    }
}
