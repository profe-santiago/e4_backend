package com.tickets.user_service.user.infrastructure.rest.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserRequest {

    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String firstName;

    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    private String lastName;

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    private String phone;

    private LocalDate birthDate;

    @Size(max = 500, message = "La URL del avatar no puede superar los 500 caracteres")
    private String avatarUrl;
}
