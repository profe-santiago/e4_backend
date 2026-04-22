package com.tickets.event_service.shared;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstracción de paginación del dominio.
 * Desacopla el dominio de Spring Data Page — las dependencias apuntan hacia adentro.
 */
public record PageResult<T>(
        List<T> items,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(
                items.stream().map(mapper).collect(Collectors.toList()),
                totalElements,
                totalPages,
                page,
                size
        );
    }
}
