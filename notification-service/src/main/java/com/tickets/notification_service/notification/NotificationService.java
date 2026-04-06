package com.tickets.notification_service.notification;

import com.tickets.notification_service.messaging.event.OrderCancelledEvent;
import com.tickets.notification_service.messaging.event.OrderConfirmedEvent;
import com.tickets.notification_service.messaging.event.PaymentCompletedEvent;
import com.tickets.notification_service.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contrato del servicio de notificaciones.
 *
 * Cada método corresponde a un evento de dominio que dispara una notificación.
 * Agregar soporte para un nuevo evento = agregar un método aquí + su consumer.
 * No se modifica código existente (OCP).
 */
public interface NotificationService {

    /**
     * Procesa el evento order.confirmed:
     * persiste la notificación y envía email de confirmación de orden al usuario.
     */
    void processOrderConfirmed(OrderConfirmedEvent event);

    /**
     * Procesa el evento payment.completed:
     * persiste la notificación y envía email de pago exitoso con detalle de tickets.
     */
    void processPaymentCompleted(PaymentCompletedEvent event);

    /**
     * Procesa el evento order.cancelled:
     * persiste la notificación y envía email de cancelación con el motivo.
     */
    void processOrderCancelled(OrderCancelledEvent event);

    /**
     * Retorna el historial paginado de notificaciones de un usuario.
     */
    Page<NotificationResponse> findByUserId(UUID userId, Pageable pageable);
}
