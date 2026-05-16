package com.tickets.event_service.tickettype.application;

import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.exception.TicketTypeNotFoundException;
import com.tickets.event_service.exception.UnauthorizedActionException;
import com.tickets.event_service.shared.UseCase;
import com.tickets.event_service.tickettype.application.dto.UpdateTicketTypeCommand;
import com.tickets.event_service.tickettype.domain.TicketType;
import com.tickets.event_service.tickettype.domain.TicketTypeRepository;

import java.util.UUID;

@UseCase
public class UpdateTicketTypeUseCase {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    public UpdateTicketTypeUseCase(EventRepository eventRepository,
                                    TicketTypeRepository ticketTypeRepository) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    public TicketType execute(UUID eventId, Long ticketTypeId, UpdateTicketTypeCommand command) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!command.isAdmin() && !event.getOrganizerId().equals(command.requesterId())) {
            throw new UnauthorizedActionException(
                    "No tenés permisos para modificar tipos de ticket de este evento");
        }

        TicketType ticketType = ticketTypeRepository.findByIdAndEventId(ticketTypeId, eventId)
                .orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));

        // La lógica de actualización vive en el dominio
        ticketType.updateDetails(command.name(), command.description(),
                command.price(), command.totalQuantity(),
                command.saleStartDate(), command.saleEndDate());

        return ticketTypeRepository.save(ticketType);
    }
}
