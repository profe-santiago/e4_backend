package com.tickets.event_service.event.application;

import com.tickets.event_service.category.domain.Category;
import com.tickets.event_service.category.domain.CategoryRepository;
import com.tickets.event_service.event.application.dto.UpdateEventCommand;
import com.tickets.event_service.event.domain.Event;
import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.exception.CategoryNotFoundException;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.exception.UnauthorizedActionException;
import com.tickets.event_service.shared.UseCase;

import java.util.UUID;

@UseCase
public class UpdateEventUseCase {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    public UpdateEventUseCase(EventRepository eventRepository,
                               CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
    }

    public Event execute(UUID eventId, UpdateEventCommand command) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        verifyOwnerOrAdmin(event.getOrganizerId(), command.requesterId(), command.isAdmin());

        Category category = command.categoryId() != null
                ? categoryRepository.findById(command.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(command.categoryId()))
                : null;

        event.update(
                command.title(),
                command.description(),
                category,
                command.venue(),
                command.city(),
                command.country(),
                command.startDate(),
                command.endDate(),
                command.imageUrl()
        );

        return eventRepository.save(event);
    }

    private void verifyOwnerOrAdmin(UUID ownerId, UUID requesterId, boolean isAdmin) {
        if (!isAdmin && !ownerId.equals(requesterId)) {
            throw new UnauthorizedActionException(
                    "No tenés permisos para modificar este evento");
        }
    }
}
