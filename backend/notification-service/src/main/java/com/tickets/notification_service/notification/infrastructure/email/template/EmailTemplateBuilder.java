package com.tickets.notification_service.notification.infrastructure.email.template;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Construye el HTML para cada tipo de email.
 * Recibe datos simples — no depende de eventos de messaging ni de dominio.
 */
@Component
public class EmailTemplateBuilder {

    public String orderConfirmed(String firstName, UUID orderId, BigDecimal total, int ticketCount) {
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
                """.formatted(firstName, orderId, orderId, total, ticketCount);
    }

    public String paymentCompleted(String firstName, UUID orderId, UUID paymentId, String stripeIntentId) {
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
                """.formatted(firstName, orderId, paymentId, stripeIntentId);
    }

    public String refundCompleted(String firstName, UUID orderId) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto;">
                  <h2 style="color: #4CAF50;">Tu reembolso fue procesado</h2>
                  <p>Hola <strong>%s</strong>,</p>
                  <p>El reembolso de tu orden <strong>#%s</strong> fue procesado exitosamente.</p>
                  <p style="margin-top:16px; color:#555;">
                    El monto sera acreditado en tu medio de pago original en los proximos dias habiles.
                  </p>
                  <hr style="border:none; border-top:1px solid #eee; margin-top:32px;"/>
                  <p style="color:#aaa; font-size:11px;">Tickets App — noreply@tickets.com</p>
                </body>
                </html>
                """.formatted(firstName, orderId);
    }

    public String refundFailed(String firstName, UUID orderId, String reason) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto;">
                  <h2 style="color: #f44336;">No pudimos procesar tu reembolso</h2>
                  <p>Hola <strong>%s</strong>,</p>
                  <p>Hubo un problema al procesar el reembolso de tu orden <strong>#%s</strong>.</p>
                  <table style="width:100%%; border-collapse:collapse; margin-top:16px;">
                    <tr style="background:#f4f4f4;">
                      <td style="padding:8px; border:1px solid #ddd;"><strong>Motivo</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">%s</td>
                    </tr>
                  </table>
                  <p style="margin-top:24px; color:#555;">
                    Tu orden sigue activa. Podés reintentar el reembolso o contactar con nuestro soporte.
                  </p>
                  <hr style="border:none; border-top:1px solid #eee; margin-top:32px;"/>
                  <p style="color:#aaa; font-size:11px;">Tickets App — noreply@tickets.com</p>
                </body>
                </html>
                """.formatted(firstName, orderId, reason);
    }

    public String orderCancelled(String firstName, UUID orderId, String reason) {
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
                """.formatted(firstName, orderId, orderId, reason);
    }
}
