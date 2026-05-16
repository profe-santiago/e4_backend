package com.tickets.notification_service.notification.application;

import com.tickets.notification_service.notification.application.dto.SendRefundFailedCommand;
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
public class SendRefundFailedNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendRefundFailedNotificationUseCase.class);

    private final NotificationRepository repository;
    private final UserGateway userGateway;
    private final NotificationEmailPort emailPort;

    public SendRefundFailedNotificationUseCase(NotificationRepository repository,
                                               UserGateway userGateway,
                                               NotificationEmailPort emailPort) {
        this.repository = repository;
        this.userGateway = userGateway;
        this.emailPort = emailPort;
    }

    public void execute(SendRefundFailedCommand command) {
        log.info("[UC] SendRefundFailed → orderId={}, userId={}", command.orderId(), command.userId());

        if (repository.existsByReferenceIdAndType(command.orderId(), NotificationType.REFUND_FAILED)) {
            log.warn("[UC] Notificación duplicada ignorada → orderId={}, type=REFUND_FAILED", command.orderId());
            return;
        }

        UserId userId = UserId.of(command.userId());
        String reason = command.reason() != null ? command.reason() : "Error interno al procesar el reembolso";

        String subject = "No pudimos procesar tu reembolso - Orden #" + command.orderId();
        String message = "Hubo un problema al reembolsar la orden #" + command.orderId() + ". Motivo: " + reason;

        Notification notification = Notification.create(
                userId,
                NotificationType.REFUND_FAILED,
                subject,
                message,
                command.orderId()
        );

        userGateway.findById(userId).ifPresentOrElse(
                user -> {
                    try {
                        emailPort.sendRefundFailed(user.email(), user.firstName(), command.orderId(), reason);
                        notification.markAsSent();
                    } catch (Exception e) {
                        log.error("[UC] Fallo al enviar email de reembolso fallido: userId={}, error={}",
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
