package com.tickets.ticket_service.ticket.application;

import com.tickets.ticket_service.exception.InvalidTicketException;
import com.tickets.ticket_service.exception.TicketNotFoundException;
import com.tickets.ticket_service.shared.UseCase;
import com.tickets.ticket_service.ticket.domain.Ticket;
import com.tickets.ticket_service.ticket.domain.TicketRepository;
import com.tickets.ticket_service.ticket.domain.TicketStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Valida el ingreso de un ticket escaneando su QR.
 * Solo admins (staff de venue) pueden ejecutar este caso de uso.
 *
 * Busca el ticket por QR, verifica que este ACTIVE y lo marca como USED.
 * Retorna el ticket actualizado para mostrar la info en el scanner.
 */
@UseCase
public class ValidateTicketUseCase {

    private static final Logger log = LoggerFactory.getLogger(ValidateTicketUseCase.class);

    private final TicketRepository ticketRepository;

    public ValidateTicketUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket execute(String qrCode) {
        Ticket ticket = ticketRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new TicketNotFoundException(qrCode));

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            log.warn("[ValidateTicket] Intento de ingreso con ticket no valido → qr={}, status={}",
                    qrCode, ticket.getStatus());
            throw new InvalidTicketException(ticket.getStatus());
        }

        ticket.markUsed();
        Ticket saved = ticketRepository.save(ticket);
        log.info("[ValidateTicket] Ingreso aprobado → ticketId={}, eventId={}", saved.getId(), saved.getEventId());

        return saved;
    }
}
