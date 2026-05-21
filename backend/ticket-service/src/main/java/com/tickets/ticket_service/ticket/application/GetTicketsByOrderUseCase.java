package com.tickets.ticket_service.ticket.application;

import com.tickets.ticket_service.shared.UseCase;
import com.tickets.ticket_service.ticket.domain.Ticket;
import com.tickets.ticket_service.ticket.domain.TicketRepository;

import java.util.List;
import java.util.UUID;

@UseCase
public class GetTicketsByOrderUseCase {

    private final TicketRepository ticketRepository;

    public GetTicketsByOrderUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> execute(UUID orderId, UUID userId, boolean isAdmin) {
        List<Ticket> tickets = ticketRepository.findAllByOrderId(orderId);
        if (isAdmin) return tickets;
        return tickets.stream()
                .filter(t -> t.getUserId().equals(userId))
                .toList();
    }
}
