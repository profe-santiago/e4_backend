package com.tickets.event_service.event.infrastructure.rest;

import com.tickets.event_service.event.application.*;
import com.tickets.event_service.event.domain.EventStatus;
import com.tickets.event_service.event.infrastructure.rest.dto.CreateEventRequest;
import com.tickets.event_service.event.infrastructure.rest.dto.EventResponse;
import com.tickets.event_service.event.infrastructure.rest.dto.UpdateEventRequest;
import com.tickets.event_service.shared.PaginatedResponse;
import com.tickets.event_service.shared.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador REST para Event.
 *
 * RESPONSABILIDAD: extraer UserId e isAdmin de Authentication y construir Commands.
 * Nunca pasa Authentication al UseCase — el dominio no conoce Spring Security.
 */
@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events")
public class EventController {

    private final CreateEventUseCase createEvent;
    private final GetEventByIdUseCase getEventById;
    private final ListPublishedEventsUseCase listPublishedEvents;
    private final ListMyEventsUseCase listMyEvents;
    private final UpdateEventUseCase updateEvent;
    private final ChangeEventStatusUseCase changeEventStatus;
    private final DeleteEventUseCase deleteEvent;
    private final EventRestMapper mapper;

    public EventController(CreateEventUseCase createEvent,
                            GetEventByIdUseCase getEventById,
                            ListPublishedEventsUseCase listPublishedEvents,
                            ListMyEventsUseCase listMyEvents,
                            UpdateEventUseCase updateEvent,
                            ChangeEventStatusUseCase changeEventStatus,
                            DeleteEventUseCase deleteEvent,
                            EventRestMapper mapper) {
        this.createEvent = createEvent;
        this.getEventById = getEventById;
        this.listPublishedEvents = listPublishedEvents;
        this.listMyEvents = listMyEvents;
        this.updateEvent = updateEvent;
        this.changeEventStatus = changeEventStatus;
        this.deleteEvent = deleteEvent;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Listar eventos publicados (paginado, público)")
    public PaginatedResponse<EventResponse> findPublished(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String venue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PaginatedResponse.from(
                listPublishedEvents.execute(categoryId, search, city, venue, page, size)
                        .map(mapper::toResponse)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener evento por ID (público)")
    public EventResponse findById(@PathVariable UUID id) {
        return mapper.toResponse(getEventById.execute(id));
    }

    @GetMapping("/my")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mis eventos como organizador")
    public List<EventResponse> findMyEvents(Authentication auth) {
        UUID organizerId = SecurityUtils.getUserId(auth);
        return listMyEvents.execute(organizerId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear evento")
    public EventResponse create(@Valid @RequestBody CreateEventRequest request,
                                 Authentication auth) {
        UUID organizerId = SecurityUtils.getUserId(auth);
        return mapper.toResponse(createEvent.execute(mapper.toCreateCommand(request, organizerId)));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar evento (organizador o ADMIN)")
    public EventResponse update(@PathVariable UUID id,
                                 @Valid @RequestBody UpdateEventRequest request,
                                 Authentication auth) {
        UUID requesterId = SecurityUtils.getUserId(auth);
        boolean isAdmin = SecurityUtils.isAdmin(auth);
        return mapper.toResponse(updateEvent.execute(id, mapper.toUpdateCommand(request, requesterId, isAdmin)));
    }

    @PatchMapping("/{id}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cambiar estado del evento (organizador o ADMIN)")
    public EventResponse changeStatus(@PathVariable UUID id,
                                       @RequestParam EventStatus status,
                                       Authentication auth) {
        UUID requesterId = SecurityUtils.getUserId(auth);
        boolean isAdmin = SecurityUtils.isAdmin(auth);
        return mapper.toResponse(changeEventStatus.execute(id, status, requesterId, isAdmin));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar evento (organizador o ADMIN)")
    public void delete(@PathVariable UUID id, Authentication auth) {
        UUID requesterId = SecurityUtils.getUserId(auth);
        boolean isAdmin = SecurityUtils.isAdmin(auth);
        deleteEvent.execute(id, requesterId, isAdmin);
    }
}
