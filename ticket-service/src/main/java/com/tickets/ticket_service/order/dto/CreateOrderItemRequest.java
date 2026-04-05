package com.tickets.ticket_service.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrderItemRequest {

    @NotNull(message = "El eventId es obligatorio")
    private UUID eventId;

    @NotNull(message = "El ticketTypeId es obligatorio")
    private Long ticketTypeId;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private int quantity;
}
