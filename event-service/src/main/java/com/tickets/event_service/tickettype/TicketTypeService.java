package com.tickets.event_service.tickettype;

import com.tickets.event_service.tickettype.dto.TicketTypeRequest;
import com.tickets.event_service.tickettype.dto.TicketTypeResponse;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface TicketTypeService {

    TicketTypeResponse create(UUID eventId, TicketTypeRequest request, Authentication auth);

    List<TicketTypeResponse> findAllByEvent(UUID eventId);

    TicketTypeResponse findById(UUID eventId, Long id);

    TicketTypeResponse update(UUID eventId, Long id, TicketTypeRequest request, Authentication auth);

    void delete(UUID eventId, Long id, Authentication auth);
}
