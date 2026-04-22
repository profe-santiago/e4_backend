package com.tickets.event_service.tickettype.infrastructure.persistence;

import com.tickets.event_service.event.infrastructure.persistence.EventJpaEntity;
import com.tickets.event_service.tickettype.domain.Money;
import com.tickets.event_service.tickettype.domain.TicketType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

/**
 * Mapper entre TicketType (dominio) y TicketTypeJpaEntity (infraestructura).
 * Usa EntityManager.getReference() para resolver la FK de Event sin cargar la entidad.
 */
@Component
public class TicketTypePersistenceMapper {

    @PersistenceContext
    private EntityManager entityManager;

    public TicketType toDomain(TicketTypeJpaEntity entity) {
        TicketType tt = new TicketType();
        tt.setId(entity.getId());
        tt.setEventId(entity.getEvent().getId());
        tt.setName(entity.getName());
        tt.setDescription(entity.getDescription());
        tt.setPrice(new Money(entity.getPrice(), entity.getPriceCurrency()));
        tt.setTotalQuantity(entity.getTotalQuantity());
        tt.setAvailableQuantity(entity.getAvailableQuantity());
        return tt;
    }

    public TicketTypeJpaEntity toJpaEntity(TicketType domain) {
        TicketTypeJpaEntity entity = new TicketTypeJpaEntity();
        entity.setId(domain.getId());
        entity.setEvent(entityManager.getReference(EventJpaEntity.class, domain.getEventId()));
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setPrice(domain.getPrice().amount());
        entity.setPriceCurrency(domain.getPrice().currency());
        entity.setTotalQuantity(domain.getTotalQuantity());
        entity.setAvailableQuantity(domain.getAvailableQuantity());
        return entity;
    }
}
