package com.tickets.ticket_service.ticket.infrastructure.rest;

import com.tickets.ticket_service.shared.SecurityUtils;
import com.tickets.ticket_service.ticket.application.GetMyTicketsUseCase;
import com.tickets.ticket_service.ticket.application.GetTicketByIdUseCase;
import com.tickets.ticket_service.ticket.infrastructure.rest.dto.TicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final GetMyTicketsUseCase getMyTickets;
    private final GetTicketByIdUseCase getTicketById;
    private final TicketRestMapper mapper;

    public TicketController(GetMyTicketsUseCase getMyTickets,
                             GetTicketByIdUseCase getTicketById,
                             TicketRestMapper mapper) {
        this.getMyTickets = getMyTickets;
        this.getTicketById = getTicketById;
        this.mapper = mapper;
    }

    @GetMapping("/my")
    @Operation(summary = "Mis tickets activos")
    public List<TicketResponse> findMyTickets(Authentication auth) {
        UUID userId = SecurityUtils.getUserId(auth);
        return getMyTickets.execute(userId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ticket por ID (incluye QR)")
    public TicketResponse findById(@PathVariable UUID id, Authentication auth) {
        UUID userId = SecurityUtils.getUserId(auth);
        boolean isAdmin = SecurityUtils.isAdmin(auth);
        return mapper.toResponse(getTicketById.execute(id, userId, isAdmin));
    }
}
