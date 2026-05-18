package com.tickets.ticket_service.ticket.infrastructure.rest;

import com.tickets.ticket_service.exception.UnauthorizedActionException;
import com.tickets.ticket_service.shared.PaginatedResponse;
import com.tickets.ticket_service.shared.SecurityUtils;
import com.tickets.ticket_service.ticket.application.GetMyTicketsUseCase;
import com.tickets.ticket_service.ticket.application.GetTicketByIdUseCase;
import com.tickets.ticket_service.ticket.application.ValidateTicketUseCase;
import com.tickets.ticket_service.ticket.domain.Ticket;
import com.tickets.ticket_service.ticket.infrastructure.rest.dto.TicketResponse;
import com.tickets.ticket_service.ticket.infrastructure.rest.dto.ValidateTicketRequest;
import com.tickets.ticket_service.ticket.infrastructure.rest.dto.ValidateTicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final GetMyTicketsUseCase getMyTickets;
    private final GetTicketByIdUseCase getTicketById;
    private final ValidateTicketUseCase validateTicket;
    private final TicketRestMapper mapper;

    public TicketController(GetMyTicketsUseCase getMyTickets,
                             GetTicketByIdUseCase getTicketById,
                             ValidateTicketUseCase validateTicket,
                             TicketRestMapper mapper) {
        this.getMyTickets = getMyTickets;
        this.getTicketById = getTicketById;
        this.validateTicket = validateTicket;
        this.mapper = mapper;
    }

    @GetMapping("/my")
    @Operation(summary = "Mis tickets (paginado)")
    public PaginatedResponse<TicketResponse> findMyTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        UUID userId = SecurityUtils.getUserId(auth);
        return PaginatedResponse.from(getMyTickets.execute(userId, page, size).map(mapper::toResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ticket por ID (incluye QR)")
    public TicketResponse findById(@PathVariable UUID id, Authentication auth) {
        UUID userId = SecurityUtils.getUserId(auth);
        boolean isAdmin = SecurityUtils.isAdmin(auth);
        return mapper.toResponse(getTicketById.execute(id, userId, isAdmin));
    }

    @PostMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Validar ticket por QR (solo admin/staff)")
    public ValidateTicketResponse validate(@Valid @RequestBody ValidateTicketRequest request,
                                           Authentication auth) {
        if (!SecurityUtils.isAdmin(auth)) {
            throw new UnauthorizedActionException("Solo el staff puede validar tickets");
        }

        Ticket ticket = validateTicket.execute(request.qrCode());

        return ValidateTicketResponse.builder()
                .ticketId(ticket.getId())
                .orderId(ticket.getOrderId())
                .eventId(ticket.getEventId())
                .ticketTypeId(ticket.getTicketTypeId())
                .userId(ticket.getUserId())
                .status(ticket.getStatus())
                .message("Ingreso aprobado")
                .purchasedAt(ticket.getPurchasedAt())
                .build();
    }
}
