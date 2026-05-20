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
                  <h2 style="color: #4CAF50;">Tu orden fue confirmada</h2>
                  <p>Hola <strong>%s</strong>,</p>
                  <p>Recibimos tu orden y el pago está siendo procesado. Recibirás otro correo cuando se confirme.</p>
                  <table style="width:100%%; border-collapse:collapse; margin-top:16px;">
                    <tr style="background:#f4f4f4;">
                      <td style="padding:8px; border:1px solid #ddd;"><strong>Número de orden</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding:8px; border:1px solid #ddd;"><strong>Total</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">$%s USD</td>
                    </tr>
                    <tr style="background:#f4f4f4;">
                      <td style="padding:8px; border:1px solid #ddd;"><strong>Entradas</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">%d</td>
                    </tr>
                  </table>
                  <hr style="border:none; border-top:1px solid #eee; margin-top:32px;"/>
                  <p style="color:#aaa; font-size:11px;">EventFlow — noreply@eventflow.com</p>
                </body>
                </html>
                """.formatted(firstName, orderId, total, ticketCount);
    }

    public String paymentCompleted(String firstName, UUID orderId, UUID paymentId, String stripeIntentId) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto;">
                  <h2 style="color: #2196F3;">Pago confirmado — tus entradas ya están disponibles</h2>
                  <p>Hola <strong>%s</strong>,</p>
                  <p>Tu pago fue procesado correctamente. Puedes ver y descargar tus entradas desde la aplicación.</p>
                  <table style="width:100%%; border-collapse:collapse; margin-top:16px;">
                    <tr style="background:#f4f4f4;">
                      <td style="padding:8px; border:1px solid #ddd;"><strong>Número de orden</strong></td>
                      <td style="padding:8px; border:1px solid #ddd;">%s</td>
                    </tr>
                  </table>
                  <p style="margin-top:24px;">
                    <a href="https://frontend-production-132ce.up.railway.app/tickets"
                       style="background:#2196F3; color:white; padding:12px 24px;
                       text-decoration:none; border-radius:4px;">Ver mis entradas</a>
                  </p>
                  <hr style="border:none; border-top:1px solid #eee; margin-top:32px;"/>
                  <p style="color:#aaa; font-size:11px;">EventFlow — noreply@eventflow.com</p>
                </body>
                </html>
                """.formatted(firstName, orderId);
    }

    public String refundCompleted(String firstName, UUID orderId) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto;">
                  <h2 style="color: #4CAF50;">Tu reembolso fue procesado</h2>
                  <p>Hola <strong>%s</strong>,</p>
                  <p>El reembolso de tu orden <strong>#%s</strong> fue procesado exitosamente.</p>
                  <p style="margin-top:16px; color:#555;">
                    El monto será acreditado en tu medio de pago original en los próximos días hábiles.
                  </p>
                  <hr style="border:none; border-top:1px solid #eee; margin-top:32px;"/>
                  <p style="color:#aaa; font-size:11px;">EventFlow — noreply@eventflow.com</p>
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
                    Tu orden sigue activa. Puedes reintentar el reembolso o contactarnos si necesitas ayuda.
                  </p>
                  <hr style="border:none; border-top:1px solid #eee; margin-top:32px;"/>
                  <p style="color:#aaa; font-size:11px;">EventFlow — noreply@eventflow.com</p>
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
                    Si crees que es un error, contáctanos y te ayudamos.
                  </p>
                  <hr style="border:none; border-top:1px solid #eee; margin-top:32px;"/>
                  <p style="color:#aaa; font-size:11px;">EventFlow — noreply@eventflow.com</p>
                </body>
                </html>
                """.formatted(firstName, orderId, orderId, reason);
    }
}
