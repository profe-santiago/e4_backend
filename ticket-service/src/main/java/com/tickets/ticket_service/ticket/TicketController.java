package com.tickets.ticket_service.ticket;

import com.tickets.ticket_service.ticket.dto.TicketResponse;
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

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/my")
    @Operation(summary = "Mis tickets activos")
    public List<TicketResponse> findMyTickets(Authentication auth) {
        return ticketService.findMyTickets(auth);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ticket por ID (incluye QR)")
    public TicketResponse findById(@PathVariable UUID id, Authentication auth) {
        return ticketService.findById(id, auth);
    }
}
