package com.tickets.event_service.event.application;

import com.tickets.event_service.category.domain.Category;
import com.tickets.event_service.category.domain.CategoryRepository;
import com.tickets.event_service.event.application.dto.CreateEventCommand;
import com.tickets.event_service.event.domain.Event;
import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.exception.CategoryNotFoundException;
import com.tickets.event_service.shared.UseCase;

@UseCase
public class CreateEventUseCase {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    public CreateEventUseCase(EventRepository eventRepository,
                               CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
    }

    public Event execute(CreateEventCommand command) {
        Category category = resolveCategory(command.categoryId());

        Event event = Event.create(
                command.organizerId(),
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

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }
}
