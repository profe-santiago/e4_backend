package com.tickets.ticket_service.order.application;

import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderEventPublisher;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.shared.UseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cancela todas las ordenes PENDING que superaron el tiempo limite de 15 minutos.
 * Publicar order.cancelled para que event-service libere el stock reservado.
 */
@UseCase
public class CancelExpiredOrdersUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelExpiredOrdersUseCase.class);
    private static final int TTL_MINUTES = 15;

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public CancelExpiredOrdersUseCase(OrderRepository orderRepository,
                                      OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(TTL_MINUTES);
        List<Order> expired = orderRepository.findExpiredPendingOrders(threshold);

        if (expired.isEmpty()) return;

        log.info("[TTL] {} orden(es) expirada(s) encontradas", expired.size());

        for (Order order : expired) {
            try {
                order.cancel();
                orderRepository.save(order);

                List<OrderEventPublisher.StockReleaseItem> stockItems = order.getItems().stream()
                        .map(i -> new OrderEventPublisher.StockReleaseItem(
                                i.getEventId(), i.getTicketTypeId(), i.getQuantity()))
                        .toList();

                eventPublisher.publishOrderCancelled(
                        order.getId(),
                        order.getUserId(),
                        "La orden expiro por inactividad",
                        stockItems
                );

                log.info("[TTL] Orden cancelada por expiracion → orderId={}", order.getId());
            } catch (Exception e) {
                log.error("[TTL] Error al cancelar orden expirada → orderId={}, error={}",
                        order.getId(), e.getMessage());
            }
        }
    }
}
