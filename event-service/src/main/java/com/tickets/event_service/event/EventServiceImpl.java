package com.tickets.event_service.event;

import com.tickets.event_service.category.Category;
import com.tickets.event_service.category.CategoryRepository;
import com.tickets.event_service.event.dto.CreateEventRequest;
import com.tickets.event_service.event.dto.EventResponse;
import com.tickets.event_service.event.dto.UpdateEventRequest;
import com.tickets.event_service.exception.CategoryNotFoundException;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.exception.InvalidEventStatusTransitionException;
import com.tickets.event_service.shared.PaginatedResponse;
import com.tickets.event_service.shared.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    public EventServiceImpl(EventRepository eventRepository, CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public EventResponse create(CreateEventRequest request, Authentication auth) {
        UUID organizerId = SecurityUtils.getUserId(auth);

        Event event = new Event();
        event.setOrganizerId(organizerId);
        applyEventFields(event, request.getTitle(), request.getDescription(),
                request.getCategoryId(), request.getVenue(), request.getCity(),
                request.getCountry(), request.getStartDate(), request.getEndDate(),
                request.getImageUrl());

        return EventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public EventResponse findById(UUID id) {
        return eventRepository.findById(id)
                .map(EventMapper::toResponse)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    @Override
    public PaginatedResponse<EventResponse> findPublished(Long categoryId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());

        Page<Event> events = categoryId != null
                ? eventRepository.findAllByStatusAndCategoryIdWithCategory(EventStatus.PUBLISHED, categoryId, pageable)
                : eventRepository.findAllByStatusWithCategory(EventStatus.PUBLISHED, pageable);

        return PaginatedResponse.from(events.map(EventMapper::toResponse));
    }

    @Override
    public List<EventResponse> findMyEvents(Authentication auth) {
        UUID organizerId = SecurityUtils.getUserId(auth);
        return eventRepository.findAllByOrganizerId(organizerId).stream()
                .map(EventMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EventResponse update(UUID id, UpdateEventRequest request, Authentication auth) {
        Event event = findEventOrThrow(id);
        SecurityUtils.verifyOwnerOrAdmin(event.getOrganizerId(), auth);

        if (request.getTitle() != null)       event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getVenue() != null)       event.setVenue(request.getVenue());
        if (request.getCity() != null)        event.setCity(request.getCity());
        if (request.getCountry() != null)     event.setCountry(request.getCountry());
        if (request.getStartDate() != null)   event.setStartDate(request.getStartDate());
        if (request.getEndDate() != null)     event.setEndDate(request.getEndDate());
        if (request.getImageUrl() != null)    event.setImageUrl(request.getImageUrl());

        if (request.getCategoryId() != null) {
            event.setCategory(resolveCategory(request.getCategoryId()));
        }

        return EventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventResponse changeStatus(UUID id, EventStatus newStatus, Authentication auth) {
        Event event = findEventOrThrow(id);
        SecurityUtils.verifyOwnerOrAdmin(event.getOrganizerId(), auth);

        if (!event.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidEventStatusTransitionException(event.getStatus(), newStatus);
        }

        event.setStatus(newStatus);
        return EventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public void delete(UUID id, Authentication auth) {
        Event event = findEventOrThrow(id);
        SecurityUtils.verifyOwnerOrAdmin(event.getOrganizerId(), auth);
        eventRepository.delete(event);
    }

    // ─── helpers privados ────────────────────────────────────────────────────

    private Event findEventOrThrow(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    private Category resolveCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    private void applyEventFields(Event event, String title, String description,
                                   Long categoryId, String venue, String city,
                                   String country, java.time.LocalDateTime startDate,
                                   java.time.LocalDateTime endDate, String imageUrl) {
        event.setTitle(title);
        event.setDescription(description);
        event.setVenue(venue);
        event.setCity(city);
        event.setCountry(country);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setImageUrl(imageUrl);

        if (categoryId != null) {
            event.setCategory(resolveCategory(categoryId));
        }
    }
}
