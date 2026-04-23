package com.tickets.event_service.tickettype.infrastructure.persistence;

import com.tickets.event_service.tickettype.domain.TicketType;
import com.tickets.event_service.tickettype.domain.TicketTypeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia — implementa el puerto TicketTypeRepository del dominio
 * usando Spring Data JPA internamente.
 */
@Repository
@Transactional(readOnly = true)
public class JpaTicketTypeRepository implements TicketTypeRepository {

    private final SpringDataTicketTypeRepository springData;
    private final TicketTypePersistenceMapper mapper;

    public JpaTicketTypeRepository(SpringDataTicketTypeRepository springData,
                                    TicketTypePersistenceMapper mapper) {
        this.springData = springData;
        this.mapper = mapper;
    }

    @Override
    public Optional<TicketType> findById(Long id) {
        return springData.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<TicketType> findByIdAndEventId(Long id, UUID eventId) {
        return springData.findByIdAndEventId(id, eventId).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<TicketType> findByIdLocked(Long id) {
        return springData.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public List<TicketType> findAllByEventId(UUID eventId) {
        return springData.findAllByEventId(eventId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public TicketType save(TicketType ticketType) {
        TicketTypeJpaEntity entity = mapper.toJpaEntity(ticketType);
        return mapper.toDomain(springData.save(entity));
    }

    @Override
    @Transactional
    public void delete(TicketType ticketType) {
        springData.findByIdAndEventId(ticketType.getId(), ticketType.getEventId())
                .ifPresent(springData::delete);
    }
}
