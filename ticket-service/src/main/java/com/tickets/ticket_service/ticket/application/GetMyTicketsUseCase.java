package com.tickets.ticket_service.ticket.application;

import com.tickets.ticket_service.shared.UseCase;
import com.tickets.ticket_service.ticket.domain.Ticket;
import com.tickets.ticket_service.ticket.domain.TicketRepository;

import java.util.List;
import java.util.UUID;

@UseCase
public class GetMyTicketsUseCase {

    private final TicketRepository ticketRepository;

    public GetMyTicketsUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> execute(UUID userId) {
        return ticketRepository.findAllByUserIdWithOrder(userId);
    }
}
