package com.tickets.ticket_service.ticket.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateTicketRequest(
        @NotBlank(message = "El codigo QR es obligatorio")
        String qrCode
) {}
