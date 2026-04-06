package com.tickets.notification_service.email;

/**
 * Contrato para el envío de emails.
 * Desacoplado de la implementación (JavaMailSender, SendGrid, SES, etc.).
 */
public interface EmailService {

    /**
     * Envía un email HTML al destinatario indicado.
     *
     * @param to      dirección de destino
     * @param subject asunto del email
     * @param html    cuerpo en formato HTML
     */
    void sendHtml(String to, String subject, String html);
}
