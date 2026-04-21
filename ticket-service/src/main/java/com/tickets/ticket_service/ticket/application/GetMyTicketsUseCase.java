package com.tickets.ticket_service.ticket.application;

import com.tickets.ticket_service.shared.PageResult;
import com.tickets.ticket_service.shared.UseCase;
import com.tickets.ticket_service.ticket.domain.Ticket;
import com.tickets.ticket_service.ticket.domain.TicketRepository;

import java.util.UUID;

@UseCase
public class GetMyTicketsUseCase {

    private final TicketRepository ticketRepository;

    public GetMyTicketsUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public PageResult<Ticket> execute(UUID userId, int page, int size) {
        return ticketRepository.findAllByUserIdWithOrder(userId, page, size);
    }
}
