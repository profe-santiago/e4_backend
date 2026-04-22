package com.tickets.notification_service.notification.application.dto;

import java.util.List;

/**
 * Abstracción de paginación de la capa de aplicación.
 * Desacopla el dominio y los use cases de Spring Data's Page.
 */
public record PagedResult<T>(
        List<T> content,
        long totalElements,
        int page,
        int size
) {
    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }
}
