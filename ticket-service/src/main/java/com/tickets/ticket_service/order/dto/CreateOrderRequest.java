package com.tickets.ticket_service.order.dto;

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

    /** ID del método de pago de Stripe (pm_xxxxx) obtenido desde el frontend con Stripe.js */
    @NotBlank(message = "El método de pago es requerido")
    private String paymentMethodId;
}
