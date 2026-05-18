package com.tickets.ticket_service.order.infrastructure.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrderRequest {

    @NotEmpty(message = "La orden debe tener al menos un item")
    @Valid
    private List<CreateOrderItemRequest> items;

    @NotBlank(message = "El ID del intento de pago es requerido")
    private String paymentIntentId;
}
