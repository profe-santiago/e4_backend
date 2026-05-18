package com.tickets.event_service.event.infrastructure.rest.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255)
    private String title;

    private String description;

    private Long categoryId;

    @NotBlank(message = "El lugar es obligatorio")
    @Size(max = 255)
    private String venue;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "El país es obligatorio")
    @Size(max = 100)
    private String country;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio no puede ser en el pasado")
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Size(max = 500)
    private String imageUrl;
}
