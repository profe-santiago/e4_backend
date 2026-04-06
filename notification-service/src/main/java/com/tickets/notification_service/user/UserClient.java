package com.tickets.notification_service.user;

import com.tickets.notification_service.user.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

/**
 * Cliente HTTP para consultar datos del user-service.
 * Usa RestClient (Spring 6.1+) — sin Feign, sin dependencias extra.
 *
 * Si el user-service no responde, devuelve Optional.empty() para que
 * el flujo de notificación decida si reintentar o registrar el fallo.
 */
@Component
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    private final RestClient restClient;

    public UserClient(@Value("${app.services.user-service.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Obtiene el perfil del usuario por ID.
     *
     * @param userId ID del usuario
     * @return Optional con el UserDto, o empty si no se pudo obtener
     */
    public Optional<UserDto> findById(UUID userId) {
        try {
            UserDto user = restClient.get()
                    .uri("/api/v1/users/{id}", userId)
                    .retrieve()
                    .body(UserDto.class);
            return Optional.ofNullable(user);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("[UserClient] Usuario no encontrado: userId={}", userId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("[UserClient] Error al consultar user-service: userId={}, error={}", userId, e.getMessage());
            return Optional.empty();
        }
    }
}
