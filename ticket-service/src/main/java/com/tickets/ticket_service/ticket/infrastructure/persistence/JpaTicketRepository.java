package com.tickets.ticket_service.ticket.infrastructure.persistence;

import com.tickets.ticket_service.ticket.domain.Ticket;
import com.tickets.ticket_service.ticket.domain.TicketRepository;
import com.tickets.ticket_service.ticket.domain.TicketStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaTicketRepository implements TicketRepository {

    private final SpringDataTicketRepository springData;
    private final TicketPersistenceMapper mapper;

    public JpaTicketRepository(SpringDataTicketRepository springData,
                                TicketPersistenceMapper mapper) {
        this.springData = springData;
        this.mapper = mapper;
    }

    @Override
    public Optional<Ticket> findById(UUID id) {
        return springData.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Ticket> findByIdWithOrder(UUID id) {
        return springData.findByIdWithOrder(id).map(mapper::toDomain);
    }

    @Override
    public List<Ticket> findAllByUserIdWithOrder(UUID userId) {
        return springData.findAllByUserIdAndStatus(userId, TicketStatus.ACTIVE)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Ticket save(Ticket ticket) {
        return mapper.toDomain(springData.save(mapper.toJpaEntity(ticket)));
    }
}
