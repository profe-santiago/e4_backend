package com.tickets.notification_service.notification.application;

import com.tickets.notification_service.notification.application.dto.SendPaymentCompletedCommand;
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
public class SendPaymentCompletedNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendPaymentCompletedNotificationUseCase.class);

    private final NotificationRepository repository;
    private final UserGateway userGateway;
    private final NotificationEmailPort emailPort;

    public SendPaymentCompletedNotificationUseCase(NotificationRepository repository,
                                                   UserGateway userGateway,
                                                   NotificationEmailPort emailPort) {
        this.repository = repository;
        this.userGateway = userGateway;
        this.emailPort = emailPort;
    }

    public void execute(SendPaymentCompletedCommand command) {
        log.info("[UC] SendPaymentCompleted → orderId={}, userId={}", command.orderId(), command.userId());

        UserId userId = UserId.of(command.userId());

        String subject = "¡Pago confirmado! Tus tickets están listos - Orden #" + command.orderId();
        String message = "Tu pago fue procesado exitosamente. ID de pago: " + command.paymentId();

        Notification notification = Notification.create(
                userId,
                NotificationType.PAYMENT_CONFIRMED,
                subject,
                message,
                command.orderId()
        );

        userGateway.findById(userId).ifPresentOrElse(
                user -> {
                    try {
                        emailPort.sendPaymentCompleted(user.email(), user.firstName(),
                                command.orderId(), command.paymentId(), command.stripePaymentIntentId());
                        notification.markAsSent();
                    } catch (Exception e) {
                        log.error("[UC] Fallo al enviar email de pago completado: userId={}, error={}",
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
