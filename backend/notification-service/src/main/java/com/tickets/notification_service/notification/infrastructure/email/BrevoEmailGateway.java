package com.tickets.notification_service.notification.infrastructure.email;

import com.tickets.notification_service.notification.domain.port.NotificationEmailPort;
import com.tickets.notification_service.notification.infrastructure.email.template.EmailTemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
class BrevoEmailGateway implements NotificationEmailPort {

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailGateway.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestClient restClient;
    private final EmailTemplateBuilder templateBuilder;
    private final String apiKey;
    private final String from;

    BrevoEmailGateway(EmailTemplateBuilder templateBuilder,
                      @Value("${brevo.api.key}") String apiKey,
                      @Value("${app.mail.from}") String from) {
        this.restClient = RestClient.create();
        this.templateBuilder = templateBuilder;
        this.apiKey = apiKey;
        this.from = from;
    }

    @Override
    public void sendOrderConfirmed(String recipientEmail, String firstName,
                                   UUID orderId, BigDecimal total, int ticketCount) {
        sendHtml(recipientEmail, "Tu orden fue confirmada - #" + orderId,
                templateBuilder.orderConfirmed(firstName, orderId, total, ticketCount));
    }

    @Override
    public void sendPaymentCompleted(String recipientEmail, String firstName,
                                     UUID orderId, UUID paymentId, String stripePaymentIntentId) {
        sendHtml(recipientEmail, "¡Pago confirmado! Tus tickets están listos - Orden #" + orderId,
                templateBuilder.paymentCompleted(firstName, orderId, paymentId, stripePaymentIntentId));
    }

    @Override
    public void sendOrderCancelled(String recipientEmail, String firstName, UUID orderId, String reason) {
        sendHtml(recipientEmail, "Tu orden fue cancelada - #" + orderId,
                templateBuilder.orderCancelled(firstName, orderId, reason));
    }

    @Override
    public void sendRefundCompleted(String recipientEmail, String firstName, UUID orderId) {
        sendHtml(recipientEmail, "Tu reembolso fue procesado - Orden #" + orderId,
                templateBuilder.refundCompleted(firstName, orderId));
    }

    @Override
    public void sendRefundFailed(String recipientEmail, String firstName, UUID orderId, String reason) {
        sendHtml(recipientEmail, "No pudimos procesar tu reembolso - Orden #" + orderId,
                templateBuilder.refundFailed(firstName, orderId, reason));
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            Map<String, Object> body = Map.of(
                "sender",      Map.of("name", "EventFlow", "email", from),
                "to",          List.of(Map.of("email", to)),
                "subject",     subject,
                "htmlContent", html
            );

            restClient.post()
                    .uri(BREVO_API_URL)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("[EmailGateway] Email enviado → to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("[EmailGateway] Fallo al enviar email → to={}, error={}", to, e.getMessage());
            throw new RuntimeException("Error al enviar email a: " + to, e);
        }
    }
}
