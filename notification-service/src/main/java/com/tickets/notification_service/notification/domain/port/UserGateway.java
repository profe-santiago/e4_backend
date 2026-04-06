package com.tickets.notification_service.notification.domain.port;

import com.tickets.notification_service.notification.domain.UserInfo;
import com.tickets.notification_service.notification.domain.UserId;

import java.util.Optional;

/**
 * Puerto secundario para obtener datos del usuario destinatario.
 * La implementación (HTTP, cache, etc.) vive en infrastructure.
 */
public interface UserGateway {

    Optional<UserInfo> findById(UserId userId);
}
