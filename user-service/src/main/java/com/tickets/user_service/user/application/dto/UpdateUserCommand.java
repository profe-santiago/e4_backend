package com.tickets.user_service.user.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando inmutable para actualización parcial de perfil.
 * Campos nulos se ignoran — solo se aplican los presentes.
 */
public record UpdateUserCommand(
        UUID userId,
        String firstName,
        String lastName,
        String phone,
        LocalDate birthDate,
        String avatarUrl
) {}
