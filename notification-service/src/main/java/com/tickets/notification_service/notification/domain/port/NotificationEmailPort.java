package com.tickets.notification_service.notification.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto secundario para envío de emails de notificación.
 *
 * Cada método tipado corresponde a un tipo de notificación.
 * La implementación construye el HTML y delega al proveedor de email.
 * El dominio no sabe nada de templates, JavaMail ni HTML.
 */
public interface NotificationEmailPort {

    void sendOrderConfirmed(String recipientEmail,
                            String firstName,
                            UUID orderId,
                            BigDecimal total,
                            int ticketCount);

    void sendPaymentCompleted(String recipientEmail,
                              String firstName,
                              UUID orderId,
                              UUID paymentId,
                              String stripePaymentIntentId);

    void sendOrderCancelled(String recipientEmail,
                            String firstName,
                            UUID orderId,
                            String reason);
}
