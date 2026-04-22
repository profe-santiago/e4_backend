package com.tickets.event_service.tickettype.application;

import com.tickets.event_service.shared.UseCase;
import com.tickets.event_service.tickettype.domain.InsufficientStockException;
import com.tickets.event_service.tickettype.domain.ReservedStockItem;
import com.tickets.event_service.tickettype.domain.StockEventPublisher;
import com.tickets.event_service.tickettype.domain.TicketType;
import com.tickets.event_service.tickettype.domain.TicketTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: reservar stock para una orden.
 *
 * Patrón de dos fases para garantizar atomicidad:
 *   Fase 1 — Validación: adquiere locks y verifica disponibilidad SIN modificar nada.
 *   Fase 2 — Aplicación: si todo pasó, aplica los decrementos y publica éxito.
 *
 * Si fase 1 falla → no hay cambios en DB → publica fallo y retorna.
 */
@UseCase
public class ReserveStockUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReserveStockUseCase.class);

    private final TicketTypeRepository ticketTypeRepository;
    private final StockEventPublisher stockEventPublisher;

    public ReserveStockUseCase(TicketTypeRepository ticketTypeRepository,
                                StockEventPublisher stockEventPublisher) {
        this.ticketTypeRepository = ticketTypeRepository;
        this.stockEventPublisher = stockEventPublisher;
    }

    @Transactional
    public void execute(UUID orderId, List<ReservationItem> items) {
        log.info("[RESERVE] Iniciando reserva → orderId={}, items={}", orderId, items.size());

        // ── Fase 1: Validación con PESSIMISTIC_WRITE lock ─────────────────────
        List<TicketType> lockedTypes = new ArrayList<>();

        for (ReservationItem item : items) {
            var opt = ticketTypeRepository.findByIdLocked(item.ticketTypeId());

            if (opt.isEmpty()) {
                String reason = "Tipo de ticket no encontrado: " + item.ticketTypeId();
                log.warn("[RESERVE] {} → orderId={}", reason, orderId);
                stockEventPublisher.publishFailed(orderId, reason);
                return;
            }

            TicketType tt = opt.get();
            if (tt.getAvailableQuantity() < item.quantity()) {
                String reason = String.format(
                        "Stock insuficiente para '%s': disponible=%d, solicitado=%d",
                        tt.getName(), tt.getAvailableQuantity(), item.quantity());
                log.warn("[RESERVE] {} → orderId={}", reason, orderId);
                stockEventPublisher.publishFailed(orderId, reason);
                return;
            }

            lockedTypes.add(tt);
        }

        // ── Fase 2: Aplicación — todos los checks pasaron ─────────────────────
        List<ReservedStockItem> reservedItems = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            ReservationItem item = items.get(i);
            TicketType tt = lockedTypes.get(i);

            // La lógica de reserva vive en el dominio
            tt.reserveStock(item.quantity());
            ticketTypeRepository.save(tt);

            reservedItems.add(new ReservedStockItem(
                    item.eventId(), item.ticketTypeId(), item.quantity(), tt.getPrice()));
        }

        stockEventPublisher.publishReserved(orderId, reservedItems);
        log.info("[RESERVE] Stock reservado exitosamente → orderId={}", orderId);
    }

    /**
     * Record de entrada para cada ítem de reserva — evita acoplar el UseCase al DTO de mensajería.
     */
    public record ReservationItem(UUID eventId, Long ticketTypeId, int quantity) {}
}
