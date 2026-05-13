package com.tickets.notification_service.notification.application;

import com.tickets.notification_service.notification.application.dto.SendOrderConfirmedCommand;
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
public class SendOrderConfirmedNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendOrderConfirmedNotificationUseCase.class);

    private final NotificationRepository repository;
    private final UserGateway userGateway;
    private final NotificationEmailPort emailPort;

    public SendOrderConfirmedNotificationUseCase(NotificationRepository repository,
                                                 UserGateway userGateway,
                                                 NotificationEmailPort emailPort) {
        this.repository = repository;
        this.userGateway = userGateway;
        this.emailPort = emailPort;
    }

    public void execute(SendOrderConfirmedCommand command) {
        log.info("[UC] SendOrderConfirmed → orderId={}, userId={}", command.orderId(), command.userId());

        if (repository.existsByReferenceIdAndType(command.orderId(), NotificationType.PURCHASE_CONFIRMATION)) {
            log.warn("[UC] Notificación duplicada ignorada → orderId={}, type=PURCHASE_CONFIRMATION", command.orderId());
            return;
        }

        UserId userId = UserId.of(command.userId());
        int ticketCount = command.tickets() != null ? command.tickets().size() : 0;

        String subject = "Tu orden fue confirmada - #" + command.orderId();
        String message = "Tu orden #" + command.orderId() + " fue confirmada por $" + command.totalAmount() + " USD.";

        Notification notification = Notification.create(
                userId,
                NotificationType.PURCHASE_CONFIRMATION,
                subject,
                message,
                command.orderId()
        );

        userGateway.findById(userId).ifPresentOrElse(
                user -> {
                    try {
                        emailPort.sendOrderConfirmed(user.email(), user.firstName(),
                                command.orderId(), command.totalAmount(), ticketCount);
                        notification.markAsSent();
                    } catch (Exception e) {
                        log.error("[UC] Fallo al enviar email de orden confirmada: userId={}, error={}",
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
