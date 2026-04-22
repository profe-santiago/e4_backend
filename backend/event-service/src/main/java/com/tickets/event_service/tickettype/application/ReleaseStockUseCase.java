package com.tickets.event_service.tickettype.application;

import com.tickets.event_service.shared.UseCase;
import com.tickets.event_service.tickettype.domain.TicketType;
import com.tickets.event_service.tickettype.domain.TicketTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso: liberar stock cuando una orden es cancelada, falla o es reembolsada.
 *
 * Usa PESSIMISTIC_WRITE lock para evitar condiciones de carrera con ReserveStockUseCase.
 * Si algún TicketType no se encuentra, loguea una advertencia y continúa con los demás.
 */
@UseCase
public class ReleaseStockUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReleaseStockUseCase.class);

    private final TicketTypeRepository ticketTypeRepository;

    public ReleaseStockUseCase(TicketTypeRepository ticketTypeRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
    }

    @Transactional
    public void execute(UUID orderId, List<ReleaseItem> items) {
        log.info("[RELEASE] Liberando stock → orderId={}, items={}", orderId, items.size());

        for (ReleaseItem item : items) {
            Optional<TicketType> opt = ticketTypeRepository.findByIdLocked(item.ticketTypeId());

            if (opt.isEmpty()) {
                log.warn("[RELEASE] TicketType no encontrado: ticketTypeId={}, orderId={}",
                        item.ticketTypeId(), orderId);
                continue;
            }

            TicketType tt = opt.get();
            tt.releaseStock(item.quantity());
            ticketTypeRepository.save(tt);

            log.debug("[RELEASE] Stock liberado: ticketType={}, cantidad={}, disponible={}",
                    tt.getName(), item.quantity(), tt.getAvailableQuantity());
        }

        log.info("[RELEASE] Stock liberado exitosamente → orderId={}", orderId);
    }

    /**
     * Record de entrada para cada ítem a liberar.
     */
    public record ReleaseItem(UUID eventId, Long ticketTypeId, int quantity) {}
}
