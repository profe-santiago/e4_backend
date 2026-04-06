package com.tickets.notification_service.email.template;

import com.tickets.notification_service.messaging.event.OrderCancelledEvent;
import com.tickets.notification_service.messaging.event.OrderConfirmedEvent;
import com.tickets.notification_service.messaging.event.PaymentCompletedEvent;
import org.springframework.stereotype.Component;

/**
 * Construye el cuerpo HTML de cada tipo de email de notificación.
 *
 * Responsabilidad única: transformar datos de evento en HTML listo para enviar.
 * Para escalar a templates Thymeleaf o Freemarker, solo se cambia esta clase.
 */
@Component
public class EmailTemplateBuilder {

    public String orderConfirmed(String firstName, OrderConfirmedEvent event) {
        int ticketCount = event.getTickets() != null ? event.getTickets().size() : 0;
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto;">
                  <h2 style="color: #4CAF50;">¡Tu orden fue confirmada! 🎟️</h2>
                  <p>Hola <strong>%s</strong>,</p>
                  <p>Tu orden <strong>#%s</strong> ha sido recibida y está siendo procesada.</p>
                  <table style="width:100%%; border-collapse:collapse; margin-top:16px;">
                    <tr style="background:#f4f4f4;">
                      <td style="padding:8px; border:1px solid #ddd;"><strong>Orden</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:8px; border:1px solid #ddd;"><strong>Total</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">$%s MXN</td>
                    </tr>
                    <tr style="background:#f4f4f4;">
                      <td style="padding:8px; border:1px solid #ddd;"><strong>Tickets</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">%d</td>
                    </tr>
                  </table>
                  <p style="margin-top:24px; color:#888; font-size:12px;">
                    Tu pago está siendo procesado. Recibirás otro correo cuando se confirme.
                  </p>
                  <hr style="border:none; border-top:1px solid #eee; margin-top:32px;"/>
                  <p style="color:#aaa; font-size:11px;">Tickets App — noreply@tickets.com</p>
                </body>
                </html>
                """.formatted(firstName, event.getOrderId(), event.getOrderId(),
                event.getTotalAmount(), ticketCount);
    }

    public String paymentCompleted(String firstName, PaymentCompletedEvent event) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto;">
                  <h2 style="color: #2196F3;">¡Pago confirmado! Tu entrada está lista 🎉</h2>
                  <p>Hola <strong>%s</strong>,</p>
                  <p>Tu pago fue procesado exitosamente. Podés ver tus tickets en la aplicación.</p>
                  <table style="width:100%%; border-collapse:collapse; margin-top:16px;">
                    <tr style="background:#f4f4f4;">
                      <td style="padding:8px; border:1px solid #ddd;"><strong>Orden</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:8px; border:1px solid #ddd;"><strong>ID de Pago</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr style="background:#f4f4f4;">
                      <td style="padding:8px; border:1px solid #ddd;"><strong>Referencia Stripe</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">%s</td>
                    </tr>
                  </table>
                  <p style="margin-top:24px;">
                    <a href="#" style="background:#2196F3; color:white; padding:12px 24px;
                       text-decoration:none; border-radius:4px;">Ver mis tickets</a>
                  </p>
                  <hr style="border:none; border-top:1px solid #eee; margin-top:32px;"/>
                  <p style="color:#aaa; font-size:11px;">Tickets App — noreply@tickets.com</p>
                </body>
                </html>
                """.formatted(firstName, event.getOrderId(), event.getPaymentId(),
                event.getStripePaymentIntentId());
    }

    public String orderCancelled(String firstName, OrderCancelledEvent event) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto;">
                  <h2 style="color: #f44336;">Tu orden fue cancelada</h2>
                  <p>Hola <strong>%s</strong>,</p>
                  <p>Lamentablemente tu orden <strong>#%s</strong> no pudo procesarse.</p>
                  <table style="width:100%%; border-collapse:collapse; margin-top:16px;">
                    <tr style="background:#f4f4f4;">
                      <td style="padding:8px; border:1px solid #ddd;"><strong>Orden</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:8px; border:1px solid #ddd;"><strong>Motivo</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">%s</td>
                    </tr>
                  </table>
                  <p style="margin-top:24px; color:#555;">
                    Si creés que es un error, contactá con nuestro soporte.
                  </p>
                  <hr style="border:none; border-top:1px solid #eee; margin-top:32px;"/>
                  <p style="color:#aaa; font-size:11px;">Tickets App — noreply@tickets.com</p>
                </body>
                </html>
                """.formatted(firstName, event.getOrderId(), event.getOrderId(),
                event.getReason() != null ? event.getReason() : "Error en el procesamiento del pago");
    }
}
