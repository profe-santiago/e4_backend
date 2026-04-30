package com.tickets.event_service.tickettype.application;

import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.exception.UnauthorizedActionException;
import com.tickets.event_service.shared.UseCase;
import com.tickets.event_service.tickettype.application.dto.CreateTicketTypeCommand;
import com.tickets.event_service.tickettype.domain.TicketType;
import com.tickets.event_service.tickettype.domain.TicketTypeRepository;

@UseCase
public class CreateTicketTypeUseCase {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    public CreateTicketTypeUseCase(EventRepository eventRepository,
                                    TicketTypeRepository ticketTypeRepository) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    public TicketType execute(CreateTicketTypeCommand command) {
        var event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        if (!command.isAdmin() && !event.getOrganizerId().equals(command.requesterId())) {
            throw new UnauthorizedActionException(
                    "No tenés permisos para agregar tipos de ticket a este evento");
        }

        TicketType ticketType = TicketType.create(
                command.eventId(),
                command.name(),
                command.description(),
                command.price(),
                command.totalQuantity(),
                command.saleStartDate(),
                command.saleEndDate()
        );

        return ticketTypeRepository.save(ticketType);
    }
}
