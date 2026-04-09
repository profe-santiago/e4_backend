package com.tickets.notification_service.notification.domain;

/**
 * Proyección de dominio del usuario destinatario.
 * Solo los datos que notification-service necesita para enviar un email.
 */
public record UserInfo(String firstName, String email) {
}
