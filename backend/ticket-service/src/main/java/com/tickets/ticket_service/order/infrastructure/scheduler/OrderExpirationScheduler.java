package com.tickets.ticket_service.order.infrastructure.scheduler;

import com.tickets.ticket_service.order.application.CancelExpiredOrdersUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler que detecta y cancela ordenes PENDING expiradas.
 * Se ejecuta cada 60 segundos. Con TTL de 15 minutos, el peor caso
 * es que una orden espere hasta 16 minutos antes de liberarse.
 */
@Component
public class OrderExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderExpirationScheduler.class);

    private final CancelExpiredOrdersUseCase cancelExpiredOrdersUseCase;

    public OrderExpirationScheduler(CancelExpiredOrdersUseCase cancelExpiredOrdersUseCase) {
        this.cancelExpiredOrdersUseCase = cancelExpiredOrdersUseCase;
    }

    @Scheduled(fixedDelay = 60_000)
    public void cancelExpiredOrders() {
        log.debug("[Scheduler] Verificando ordenes expiradas...");
        cancelExpiredOrdersUseCase.execute();
    }
}
