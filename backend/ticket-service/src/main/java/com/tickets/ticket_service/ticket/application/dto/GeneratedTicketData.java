package com.tickets.ticket_service.ticket.application.dto;

import java.util.UUID;

/**
 * Datos de un ticket generado — retornado por GenerateTicketsUseCase.
 * Usado por ConfirmOrderUseCase para construir el OrderConfirmedEvent.
 */
public record GeneratedTicketData(
        UUID ticketId,
        UUID eventId,
        Long ticketTypeId,
        String qrCode
) {}
