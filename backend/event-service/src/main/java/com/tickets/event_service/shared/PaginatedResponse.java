package com.tickets.event_service.shared;

import java.util.List;

/**
 * Wrapper genérico para respuestas paginadas HTTP.
 * Construido a partir de PageResult<T> (dominio) — nunca desde Spring Data Page.
 */
public record PaginatedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PaginatedResponse<T> from(PageResult<T> result) {
        return new PaginatedResponse<>(
                result.items(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.page() >= result.totalPages() - 1
        );
    }
}
