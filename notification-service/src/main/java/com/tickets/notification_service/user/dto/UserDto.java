package com.tickets.notification_service.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Proyección mínima del user-service para obtener el email del destinatario.
 */
@Data
@NoArgsConstructor
public class UserDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}
