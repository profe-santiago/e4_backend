package com.tickets.ticket_service.ticket.application;

import com.tickets.ticket_service.shared.UseCase;
import com.tickets.ticket_service.shared.QrCodeGenerator;
import com.tickets.ticket_service.ticket.application.dto.GenerateTicketsCommand;
import com.tickets.ticket_service.ticket.application.dto.GeneratedTicketData;
import com.tickets.ticket_service.ticket.domain.Ticket;
import com.tickets.ticket_service.ticket.domain.TicketRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso: generar tickets físicos para una orden confirmada.
 * Un ticket por unidad de cada ítem (quantity=2 → 2 tickets).
 */
@UseCase
public class GenerateTicketsUseCase {

    private final TicketRepository ticketRepository;

    public GenerateTicketsUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<GeneratedTicketData> execute(GenerateTicketsCommand command) {
        List<GeneratedTicketData> result = new ArrayList<>();

        for (GenerateTicketsCommand.OrderItemData item : command.items()) {
            for (int i = 0; i < item.quantity(); i++) {
                Ticket ticket = Ticket.create(
                        item.orderItemId(),
                        command.userId(),
                        item.eventId(),
                        item.ticketTypeId(),
                        QrCodeGenerator.generate()
                );
                Ticket saved = ticketRepository.save(ticket);

                result.add(new GeneratedTicketData(
                        saved.getId(),
                        saved.getEventId(),
                        saved.getTicketTypeId(),
                        saved.getQrCode()
                ));
            }
        }

        return result;
    }
}
