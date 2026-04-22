package com.tickets.notification_service.notification.infrastructure.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UserDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}
