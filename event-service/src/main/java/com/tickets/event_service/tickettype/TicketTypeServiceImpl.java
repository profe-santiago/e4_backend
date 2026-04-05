package com.tickets.event_service.tickettype;

import com.tickets.event_service.event.Event;
import com.tickets.event_service.event.EventRepository;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.exception.TicketTypeNotFoundException;
import com.tickets.event_service.shared.SecurityUtils;
import com.tickets.event_service.tickettype.dto.TicketTypeRequest;
import com.tickets.event_service.tickettype.dto.TicketTypeResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    public TicketTypeServiceImpl(TicketTypeRepository ticketTypeRepository,
                                  EventRepository eventRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public TicketTypeResponse create(UUID eventId, TicketTypeRequest request, Authentication auth) {
        Event event = findEventOrThrow(eventId);
        SecurityUtils.verifyOwnerOrAdmin(event.getOrganizerId(), auth);

        TicketType ticketType = new TicketType();
        ticketType.setEvent(event);
        applyFields(ticketType, request);

        return TicketTypeMapper.toResponse(ticketTypeRepository.save(ticketType));
    }

    @Override
    public List<TicketTypeResponse> findAllByEvent(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException(eventId);
        }
        return ticketTypeRepository.findAllByEventId(eventId).stream()
                .map(TicketTypeMapper::toResponse)
                .toList();
    }

    @Override
    public TicketTypeResponse findById(UUID eventId, Long id) {
        return ticketTypeRepository.findByIdAndEventId(id, eventId)
                .map(TicketTypeMapper::toResponse)
                .orElseThrow(() -> new TicketTypeNotFoundException(id));
    }

    @Override
    @Transactional
    public TicketTypeResponse update(UUID eventId, Long id, TicketTypeRequest request, Authentication auth) {
        Event event = findEventOrThrow(eventId);
        SecurityUtils.verifyOwnerOrAdmin(event.getOrganizerId(), auth);

        TicketType ticketType = ticketTypeRepository.findByIdAndEventId(id, eventId)
                .orElseThrow(() -> new TicketTypeNotFoundException(id));

        applyFields(ticketType, request);
        return TicketTypeMapper.toResponse(ticketTypeRepository.save(ticketType));
    }

    @Override
    @Transactional
    public void delete(UUID eventId, Long id, Authentication auth) {
        Event event = findEventOrThrow(eventId);
        SecurityUtils.verifyOwnerOrAdmin(event.getOrganizerId(), auth);

        TicketType ticketType = ticketTypeRepository.findByIdAndEventId(id, eventId)
                .orElseThrow(() -> new TicketTypeNotFoundException(id));

        ticketTypeRepository.delete(ticketType);
    }

    // ─── helpers privados ────────────────────────────────────────────────────

    private Event findEventOrThrow(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    private void applyFields(TicketType ticketType, TicketTypeRequest request) {
        ticketType.setName(request.getName());
        ticketType.setDescription(request.getDescription());
        ticketType.setPrice(request.getPrice());
        ticketType.setTotalQuantity(request.getTotalQuantity());
        ticketType.setAvailableQuantity(request.getTotalQuantity());
    }
}
