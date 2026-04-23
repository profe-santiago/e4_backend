package com.tickets.event_service.event.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command de entrada para actualizar un evento.
 * Los campos nulos indican "no cambiar" — la lógica de merge vive en Event.update().
 */
public record UpdateEventCommand(
        UUID requesterId,
        boolean isAdmin,
        String title,
        String description,
        Long categoryId,
        String venue,
        String city,
        String country,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String imageUrl
) {}
