package com.tickets.notification_service.notification.infrastructure.email;

import com.tickets.notification_service.notification.domain.port.NotificationEmailPort;
import com.tickets.notification_service.notification.infrastructure.email.template.EmailTemplateBuilder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Adaptador de salida — implementa el puerto de email usando JavaMailSender.
 * Construye el HTML con EmailTemplateBuilder y lo envía vía SMTP.
 */
@Component
class JavaMailEmailGateway implements NotificationEmailPort {

    private static final Logger log = LoggerFactory.getLogger(JavaMailEmailGateway.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder templateBuilder;
    private final String from;

    JavaMailEmailGateway(JavaMailSender mailSender,
                         EmailTemplateBuilder templateBuilder,
                         @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.templateBuilder = templateBuilder;
        this.from = from;
    }

    @Override
    public void sendOrderConfirmed(String recipientEmail, String firstName,
                                   UUID orderId, BigDecimal total, int ticketCount) {
        String subject = "Tu orden fue confirmada - #" + orderId;
        String html = templateBuilder.orderConfirmed(firstName, orderId, total, ticketCount);
        sendHtml(recipientEmail, subject, html);
    }

    @Override
    public void sendPaymentCompleted(String recipientEmail, String firstName,
                                     UUID orderId, UUID paymentId, String stripePaymentIntentId) {
        String subject = "¡Pago confirmado! Tus tickets están listos - Orden #" + orderId;
        String html = templateBuilder.paymentCompleted(firstName, orderId, paymentId, stripePaymentIntentId);
        sendHtml(recipientEmail, subject, html);
    }

    @Override
    public void sendOrderCancelled(String recipientEmail, String firstName, UUID orderId, String reason) {
        String subject = "Tu orden fue cancelada - #" + orderId;
        String html = templateBuilder.orderCancelled(firstName, orderId, reason);
        sendHtml(recipientEmail, subject, html);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[EmailGateway] Email enviado → to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("[EmailGateway] Fallo al enviar email → to={}, error={}", to, e.getMessage());
            throw new RuntimeException("Error al enviar email a: " + to, e);
        }
    }
}
