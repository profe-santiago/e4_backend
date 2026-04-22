package com.tickets.ticket_service.ticket.infrastructure.persistence;

import com.tickets.ticket_service.order.infrastructure.persistence.OrderItemJpaEntity;
import com.tickets.ticket_service.ticket.domain.Ticket;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

/**
 * Mapper entre Ticket (dominio) y TicketJpaEntity.
 *
 * Para el campo orderId (denormalizado): se navega la relación JPA
 * ticket.orderItem.order.id — requiere que la query use JOIN FETCH.
 *
 * Para FK de escritura: usa EntityManager.getReference() para evitar N+1.
 */
@Component
public class TicketPersistenceMapper {

    @PersistenceContext
    private EntityManager entityManager;

    public Ticket toDomain(TicketJpaEntity entity) {
        Ticket ticket = new Ticket();
        ticket.setId(entity.getId());
        ticket.setOrderItemId(entity.getOrderItem().getId());
        ticket.setUserId(entity.getUserId());
        ticket.setEventId(entity.getEventId());
        ticket.setTicketTypeId(entity.getTicketTypeId());
        ticket.setQrCode(entity.getQrCode());
        ticket.setStatus(entity.getStatus());
        ticket.setPurchasedAt(entity.getPurchasedAt());
        ticket.setUsedAt(entity.getUsedAt());

        // orderId denormalizado — navega la relación JPA (cargada via JOIN FETCH)
        if (entity.getOrderItem().getOrder() != null) {
            ticket.setOrderId(entity.getOrderItem().getOrder().getId());
        }

        return ticket;
    }

    public TicketJpaEntity toJpaEntity(Ticket domain) {
        TicketJpaEntity entity = new TicketJpaEntity();
        entity.setId(domain.getId());
        entity.setOrderItem(
                entityManager.getReference(OrderItemJpaEntity.class, domain.getOrderItemId()));
        entity.setUserId(domain.getUserId());
        entity.setEventId(domain.getEventId());
        entity.setTicketTypeId(domain.getTicketTypeId());
        entity.setQrCode(domain.getQrCode());
        entity.setStatus(domain.getStatus());
        entity.setPurchasedAt(domain.getPurchasedAt());
        entity.setUsedAt(domain.getUsedAt());
        return entity;
    }
}
