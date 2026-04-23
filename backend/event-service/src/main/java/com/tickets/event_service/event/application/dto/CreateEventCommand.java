package com.tickets.event_service.event.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Command de entrada para crear un evento.
 * Record inmutable — el UseCase no toca objetos HTTP ni Authentication.
 */
public record CreateEventCommand(
        UUID organizerId,
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
