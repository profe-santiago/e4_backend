package com.tickets.event_service.shared;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper genérico para respuestas paginadas.
 * Evita que cada módulo defina su propia estructura de paginación.
 */
public record PaginatedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PaginatedResponse<T> from(Page<T> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
