package com.tickets.event_service.tickettype.infrastructure.rest.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TicketTypeRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal price;

    @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 3, message = "El código de moneda debe tener exactamente 3 caracteres")
    private String currency = "USD";

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int totalQuantity;

    private LocalDateTime saleStartDate;

    private LocalDateTime saleEndDate;
}
