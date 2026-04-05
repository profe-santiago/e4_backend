package com.tickets.ticket_service.ticket;

import com.tickets.ticket_service.exception.TicketNotFoundException;
import com.tickets.ticket_service.messaging.event.OrderConfirmedEvent;
import com.tickets.ticket_service.order.Order;
import com.tickets.ticket_service.order.OrderItem;
import com.tickets.ticket_service.shared.QrCodeGenerator;
import com.tickets.ticket_service.shared.SecurityUtils;
import com.tickets.ticket_service.ticket.dto.TicketResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional
    public List<OrderConfirmedEvent.ConfirmedTicket> generateTickets(Order order) {
        List<OrderConfirmedEvent.ConfirmedTicket> result = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                Ticket ticket = new Ticket();
                ticket.setOrderItem(item);
                ticket.setUserId(order.getUserId());
                ticket.setEventId(item.getEventId());
                ticket.setTicketTypeId(item.getTicketTypeId());
                ticket.setQrCode(QrCodeGenerator.generate());
                ticket.setStatus(TicketStatus.ACTIVE);

                Ticket saved = ticketRepository.save(ticket);

                result.add(new OrderConfirmedEvent.ConfirmedTicket(
                        saved.getId(),
                        saved.getEventId(),
                        saved.getTicketTypeId(),
                        saved.getQrCode()
                ));
            }
        }

        return result;
    }

    @Override
    public List<TicketResponse> findMyTickets(Authentication auth) {
        UUID userId = SecurityUtils.getUserId(auth);
        return ticketRepository.findAllByUserIdWithOrder(userId).stream()
                .map(TicketMapper::toResponse)
                .toList();
    }

    @Override
    public TicketResponse findById(UUID id, Authentication auth) {
        Ticket ticket = ticketRepository.findByIdWithOrder(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        SecurityUtils.verifyOwnerOrAdmin(ticket.getUserId(), auth);
        return TicketMapper.toResponse(ticket);
    }
}
