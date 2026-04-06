package com.tickets.notification_service.notification;

import com.tickets.notification_service.email.EmailService;
import com.tickets.notification_service.email.template.EmailTemplateBuilder;
import com.tickets.notification_service.messaging.event.OrderCancelledEvent;
import com.tickets.notification_service.messaging.event.OrderConfirmedEvent;
import com.tickets.notification_service.messaging.event.PaymentCompletedEvent;
import com.tickets.notification_service.notification.dto.NotificationResponse;
import com.tickets.notification_service.user.UserClient;
import com.tickets.notification_service.user.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementación del servicio de notificaciones.
 *
 * Coordina tres responsabilidades distintas:
 *   1. UserClient     → obtiene el email del destinatario
 *   2. EmailService   → envía el correo HTML
 *   3. Repository     → persiste el registro de la notificación
 *
 * Si el usuario no se encuentra o el email falla, la notificación queda en FAILED
 * para ser revisada o reintentar desde la DLQ.
 */
@Service
class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository repository;
    private final NotificationMapper mapper;
    private final UserClient userClient;
    private final EmailService emailService;
    private final EmailTemplateBuilder templateBuilder;

    NotificationServiceImpl(NotificationRepository repository,
                            NotificationMapper mapper,
                            UserClient userClient,
                            EmailService emailService,
                            EmailTemplateBuilder templateBuilder) {
        this.repository = repository;
        this.mapper = mapper;
        this.userClient = userClient;
        this.emailService = emailService;
        this.templateBuilder = templateBuilder;
    }

    @Override
    @Transactional
    public void processOrderConfirmed(OrderConfirmedEvent event) {
        log.info("[NotificationService] processOrderConfirmed → orderId={}, userId={}",
                event.getOrderId(), event.getUserId());

        String subject = "Tu orden fue confirmada - #" + event.getOrderId();
        String message = "Tu orden #" + event.getOrderId() + " fue confirmada por $" + event.getTotalAmount() + " MXN.";

        Notification notification = buildNotification(
                event.getUserId(),
                NotificationType.PURCHASE_CONFIRMATION,
                subject,
                message,
                event.getOrderId()
        );

        sendAndPersist(notification, event.getUserId(), subject,
                user -> templateBuilder.orderConfirmed(user.getFirstName(), event));
    }

    @Override
    @Transactional
    public void processPaymentCompleted(PaymentCompletedEvent event) {
        log.info("[NotificationService] processPaymentCompleted → orderId={}, userId={}",
                event.getOrderId(), event.getUserId());

        String subject = "¡Pago confirmado! Tus tickets están listos - Orden #" + event.getOrderId();
        String message = "Tu pago fue procesado exitosamente. ID de pago: " + event.getPaymentId();

        Notification notification = buildNotification(
                event.getUserId(),
                NotificationType.PURCHASE_CONFIRMATION,
                subject,
                message,
                event.getOrderId()
        );

        sendAndPersist(notification, event.getUserId(), subject,
                user -> templateBuilder.paymentCompleted(user.getFirstName(), event));
    }

    @Override
    @Transactional
    public void processOrderCancelled(OrderCancelledEvent event) {
        log.info("[NotificationService] processOrderCancelled → orderId={}, userId={}",
                event.getOrderId(), event.getUserId());

        String reason = event.getReason() != null ? event.getReason() : "Error en el procesamiento";
        String subject = "Tu orden fue cancelada - #" + event.getOrderId();
        String message = "Tu orden #" + event.getOrderId() + " fue cancelada. Motivo: " + reason;

        Notification notification = buildNotification(
                event.getUserId(),
                NotificationType.EVENT_CANCELLED,
                subject,
                message,
                event.getOrderId()
        );

        sendAndPersist(notification, event.getUserId(), subject,
                user -> templateBuilder.orderCancelled(user.getFirstName(), event));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> findByUserId(UUID userId, Pageable pageable) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(mapper::toResponse);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void sendAndPersist(Notification notification,
                                UUID userId,
                                String subject,
                                java.util.function.Function<UserDto, String> htmlBuilder) {
        userClient.findById(userId).ifPresentOrElse(
                user -> {
                    try {
                        String html = htmlBuilder.apply(user);
                        emailService.sendHtml(user.getEmail(), subject, html);
                        notification.setStatus(NotificationStatus.SENT);
                        notification.setSentAt(LocalDateTime.now());
                    } catch (Exception e) {
                        log.error("[NotificationService] Fallo al enviar email: userId={}, error={}",
                                userId, e.getMessage());
                        notification.setStatus(NotificationStatus.FAILED);
                    }
                },
                () -> {
                    log.warn("[NotificationService] Usuario no encontrado, notificación marcada como FAILED: userId={}", userId);
                    notification.setStatus(NotificationStatus.FAILED);
                }
        );
        repository.save(notification);
    }

    private Notification buildNotification(UUID userId,
                                           NotificationType type,
                                           String subject,
                                           String message,
                                           UUID referenceId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setSubject(subject);
        n.setMessage(message);
        n.setReferenceId(referenceId);
        n.setStatus(NotificationStatus.PENDING);
        return n;
    }
}
