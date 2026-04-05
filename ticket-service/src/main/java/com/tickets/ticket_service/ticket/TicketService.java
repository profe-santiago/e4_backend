package com.tickets.ticket_service.ticket;

import com.tickets.ticket_service.messaging.event.OrderConfirmedEvent;
import com.tickets.ticket_service.order.Order;
import com.tickets.ticket_service.ticket.dto.TicketResponse;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface TicketService {

    /**
     * Genera los tickets físicos para una orden confirmada.
     * Devuelve los datos mínimos necesarios para publicar OrderConfirmedEvent.
     */
    List<OrderConfirmedEvent.ConfirmedTicket> generateTickets(Order order);

    List<TicketResponse> findMyTickets(Authentication auth);

    TicketResponse findById(UUID id, Authentication auth);
}
