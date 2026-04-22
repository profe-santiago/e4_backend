package com.tickets.ticket_service.ticket.application;

import com.tickets.ticket_service.exception.TicketNotFoundException;
import com.tickets.ticket_service.exception.UnauthorizedActionException;
import com.tickets.ticket_service.shared.UseCase;
import com.tickets.ticket_service.ticket.domain.Ticket;
import com.tickets.ticket_service.ticket.domain.TicketRepository;

import java.util.UUID;

@UseCase
public class GetTicketByIdUseCase {

    private final TicketRepository ticketRepository;

    public GetTicketByIdUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket execute(UUID ticketId, UUID requesterId, boolean isAdmin) {
        Ticket ticket = ticketRepository.findByIdWithOrder(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (!isAdmin && !ticket.getUserId().equals(requesterId)) {
            throw new UnauthorizedActionException("No tenés permisos para ver este ticket");
        }

        return ticket;
    }
}
