package com.tickets.user_service.user.application.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando inmutable para crear un usuario.
 * El userId proviene del JWT (extraído en el adaptador REST).
 */
public record CreateUserCommand(
        UUID userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate birthDate,
        String avatarUrl
) {}
