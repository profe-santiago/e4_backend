package com.tickets.notification_service.notification.infrastructure.user;

import com.tickets.notification_service.notification.domain.UserInfo;
import com.tickets.notification_service.notification.domain.UserId;
import com.tickets.notification_service.notification.domain.port.UserGateway;
import com.tickets.notification_service.notification.infrastructure.user.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Adaptador de salida — implementa UserGateway consultando el user-service via HTTP.
 */
@Component
class UserHttpGateway implements UserGateway {

    private static final Logger log = LoggerFactory.getLogger(UserHttpGateway.class);

    private final RestClient restClient;

    UserHttpGateway(@Value("${app.services.user-service.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public Optional<UserInfo> findById(UserId userId) {
        try {
            UserDto dto = restClient.get()
                    .uri("/api/v1/internal/users/{id}", userId.value())
                    .retrieve()
                    .body(UserDto.class);
            if (dto == null) return Optional.empty();
            return Optional.of(new UserInfo(dto.getFirstName(), dto.getEmail()));
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("[UserGateway] Usuario no encontrado: userId={}", userId.value());
            return Optional.empty();
        } catch (Exception e) {
            log.error("[UserGateway] Error al consultar user-service: userId={}, error={}", userId.value(), e.getMessage());
            return Optional.empty();
        }
    }
}
