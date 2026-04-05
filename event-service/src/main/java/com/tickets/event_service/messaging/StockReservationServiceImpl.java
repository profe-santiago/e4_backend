package com.tickets.event_service.messaging;

import com.tickets.event_service.messaging.event.*;
import com.tickets.event_service.messaging.publisher.StockResultPublisher;
import com.tickets.event_service.tickettype.TicketType;
import com.tickets.event_service.tickettype.TicketTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lógica de reserva de stock con PESSIMISTIC WRITE LOCK.
 *
 * Patrón de dos fases:
 *   Fase 1 — Validación: adquiere locks y verifica disponibilidad SIN modificar nada.
 *   Fase 2 — Aplicación: si todo pasó, aplica los decrementos y publica éxito.
 *
 * Si fase 1 falla → no hay cambios en DB → publica fallo y retorna.
 * Esto evita rollbacks parciales y garantiza atomicidad.
 */
@Service
public class StockReservationServiceImpl implements StockReservationService {

    private static final Logger log = LoggerFactory.getLogger(StockReservationServiceImpl.class);

    private final TicketTypeRepository ticketTypeRepository;
    private final StockResultPublisher  stockResultPublisher;

    public StockReservationServiceImpl(TicketTypeRepository ticketTypeRepository,
                                        StockResultPublisher stockResultPublisher) {
        this.ticketTypeRepository = ticketTypeRepository;
        this.stockResultPublisher  = stockResultPublisher;
    }

    @Override
    @Transactional
    public void reserve(StockReserveCommand command) {
        log.info("[RESERVE] Iniciando reserva → orderId={}, items={}",
                command.getOrderId(), command.getItems().size());

        // ── Fase 1: Validación con PESSIMISTIC_WRITE lock ─────────────────────
        List<TicketType> lockedTypes = new ArrayList<>();

        for (StockReserveItem item : command.getItems()) {
            Optional<TicketType> opt = ticketTypeRepository.findByIdForUpdate(item.getTicketTypeId());

            if (opt.isEmpty()) {
                String reason = "Tipo de ticket no encontrado: " + item.getTicketTypeId();
                log.warn("[RESERVE] {} → orderId={}", reason, command.getOrderId());
                stockResultPublisher.publishFailed(new StockFailedEvent(command.getOrderId(), reason));
                return; // Sin cambios en DB → transacción limpia
            }

            TicketType tt = opt.get();
            if (tt.getAvailableQuantity() < item.getQuantity()) {
                String reason = String.format(
                        "Stock insuficiente para '%s': disponible=%d, solicitado=%d",
                        tt.getName(), tt.getAvailableQuantity(), item.getQuantity());
                log.warn("[RESERVE] {} → orderId={}", reason, command.getOrderId());
                stockResultPublisher.publishFailed(new StockFailedEvent(command.getOrderId(), reason));
                return; // Sin cambios en DB → transacción limpia
            }

            lockedTypes.add(tt);
        }

        // ── Fase 2: Aplicación — todos los checks pasaron ─────────────────────
        List<StockReservedItem> reservedItems = new ArrayList<>();

        for (int i = 0; i < command.getItems().size(); i++) {
            StockReserveItem item = command.getItems().get(i);
            TicketType tt = lockedTypes.get(i);

            tt.setAvailableQuantity(tt.getAvailableQuantity() - item.getQuantity());
            ticketTypeRepository.save(tt);

            reservedItems.add(new StockReservedItem(
                    item.getEventId(), item.getTicketTypeId(),
                    item.getQuantity(), tt.getPrice()));
        }

        stockResultPublisher.publishReserved(
                new StockReservedEvent(command.getOrderId(), reservedItems));

        log.info("[RESERVE] Stock reservado exitosamente → orderId={}", command.getOrderId());
    }
}
