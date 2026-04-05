package com.tickets.event_service.tickettype;

import com.tickets.event_service.tickettype.dto.TicketTypeRequest;
import com.tickets.event_service.tickettype.dto.TicketTypeResponse;
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
@RequestMapping("/api/v1/events/{eventId}/ticket-types")
@Tag(name = "Ticket Types")
@SecurityRequirement(name = "bearerAuth")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    public TicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de ticket de un evento")
    public List<TicketTypeResponse> findAll(@PathVariable UUID eventId) {
        return ticketTypeService.findAllByEvent(eventId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de ticket por ID")
    public TicketTypeResponse findById(@PathVariable UUID eventId, @PathVariable Long id) {
        return ticketTypeService.findById(eventId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear tipo de ticket (organizador o ADMIN)")
    public TicketTypeResponse create(
            @PathVariable UUID eventId,
            @Valid @RequestBody TicketTypeRequest request,
            Authentication auth
    ) {
        return ticketTypeService.create(eventId, request, auth);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de ticket (organizador o ADMIN)")
    public TicketTypeResponse update(
            @PathVariable UUID eventId,
            @PathVariable Long id,
            @Valid @RequestBody TicketTypeRequest request,
            Authentication auth
    ) {
        return ticketTypeService.update(eventId, id, request, auth);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar tipo de ticket (organizador o ADMIN)")
    public void delete(@PathVariable UUID eventId, @PathVariable Long id, Authentication auth) {
        ticketTypeService.delete(eventId, id, auth);
    }
}
