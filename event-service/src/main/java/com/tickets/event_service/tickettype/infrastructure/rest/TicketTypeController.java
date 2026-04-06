package com.tickets.event_service.tickettype.infrastructure.rest;

import com.tickets.event_service.shared.SecurityUtils;
import com.tickets.event_service.tickettype.application.*;
import com.tickets.event_service.tickettype.infrastructure.rest.dto.TicketTypeRequest;
import com.tickets.event_service.tickettype.infrastructure.rest.dto.TicketTypeResponse;
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
 * Adaptador REST para TicketType.
 * Extrae UserId e isAdmin de Authentication — el dominio nunca ve Spring Security.
 */
@RestController
@RequestMapping("/api/v1/events/{eventId}/ticket-types")
@Tag(name = "Ticket Types")
@SecurityRequirement(name = "bearerAuth")
public class TicketTypeController {

    private final CreateTicketTypeUseCase createTicketType;
    private final GetTicketTypeUseCase getTicketType;
    private final ListTicketTypesUseCase listTicketTypes;
    private final UpdateTicketTypeUseCase updateTicketType;
    private final DeleteTicketTypeUseCase deleteTicketType;
    private final TicketTypeRestMapper mapper;

    public TicketTypeController(CreateTicketTypeUseCase createTicketType,
                                 GetTicketTypeUseCase getTicketType,
                                 ListTicketTypesUseCase listTicketTypes,
                                 UpdateTicketTypeUseCase updateTicketType,
                                 DeleteTicketTypeUseCase deleteTicketType,
                                 TicketTypeRestMapper mapper) {
        this.createTicketType = createTicketType;
        this.getTicketType = getTicketType;
        this.listTicketTypes = listTicketTypes;
        this.updateTicketType = updateTicketType;
        this.deleteTicketType = deleteTicketType;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de ticket de un evento")
    public List<TicketTypeResponse> findAll(@PathVariable UUID eventId) {
        return listTicketTypes.execute(eventId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de ticket por ID")
    public TicketTypeResponse findById(@PathVariable UUID eventId, @PathVariable Long id) {
        return mapper.toResponse(getTicketType.execute(eventId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear tipo de ticket (organizador o ADMIN)")
    public TicketTypeResponse create(@PathVariable UUID eventId,
                                      @Valid @RequestBody TicketTypeRequest request,
                                      Authentication auth) {
        UUID requesterId = SecurityUtils.getUserId(auth);
        boolean isAdmin = SecurityUtils.isAdmin(auth);
        return mapper.toResponse(
                createTicketType.execute(mapper.toCreateCommand(eventId, request, requesterId, isAdmin))
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de ticket (organizador o ADMIN)")
    public TicketTypeResponse update(@PathVariable UUID eventId,
                                      @PathVariable Long id,
                                      @Valid @RequestBody TicketTypeRequest request,
                                      Authentication auth) {
        UUID requesterId = SecurityUtils.getUserId(auth);
        boolean isAdmin = SecurityUtils.isAdmin(auth);
        return mapper.toResponse(
                updateTicketType.execute(eventId, id, mapper.toUpdateCommand(request, requesterId, isAdmin))
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar tipo de ticket (organizador o ADMIN)")
    public void delete(@PathVariable UUID eventId,
                        @PathVariable Long id,
                        Authentication auth) {
        UUID requesterId = SecurityUtils.getUserId(auth);
        boolean isAdmin = SecurityUtils.isAdmin(auth);
        deleteTicketType.execute(eventId, id, requesterId, isAdmin);
    }
}
