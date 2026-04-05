package com.tickets.event_service.event;

import com.tickets.event_service.event.dto.CreateEventRequest;
import com.tickets.event_service.event.dto.EventResponse;
import com.tickets.event_service.event.dto.UpdateEventRequest;
import com.tickets.event_service.shared.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Operation(summary = "Listar eventos publicados (paginado, público)")
    public PaginatedResponse<EventResponse> findPublished(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return eventService.findPublished(categoryId, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener evento por ID (público)")
    public EventResponse findById(@PathVariable UUID id) {
        return eventService.findById(id);
    }

    @GetMapping("/my")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mis eventos como organizador")
    public List<EventResponse> findMyEvents(Authentication auth) {
        return eventService.findMyEvents(auth);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear evento")
    public EventResponse create(@Valid @RequestBody CreateEventRequest request, Authentication auth) {
        return eventService.create(request, auth);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar evento (organizador o ADMIN)")
    public EventResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request,
            Authentication auth
    ) {
        return eventService.update(id, request, auth);
    }

    @PatchMapping("/{id}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cambiar estado del evento (organizador o ADMIN)")
    public EventResponse changeStatus(
            @PathVariable UUID id,
            @RequestParam EventStatus status,
            Authentication auth
    ) {
        return eventService.changeStatus(id, status, auth);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar evento (organizador o ADMIN)")
    public void delete(@PathVariable UUID id, Authentication auth) {
        eventService.delete(id, auth);
    }
}
