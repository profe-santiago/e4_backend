package com.tickets.ticket_service.ticket.infrastructure.persistence;

import com.tickets.ticket_service.shared.PageResult;
import com.tickets.ticket_service.ticket.domain.Ticket;
import com.tickets.ticket_service.ticket.domain.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public PageResult<Ticket> findAllByUserIdWithOrder(UUID userId, int page, int size) {
        Page<TicketJpaEntity> result = springData.findAllByUserId(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "purchasedAt")));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                page,
                size
        );
    }

    @Override
    public List<Ticket> findAllByOrderId(UUID orderId) {
        return springData.findAllByOrderId(orderId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Ticket> findByQrCode(String qrCode) {
        return springData.findByQrCode(qrCode).map(mapper::toDomain);
    }

    @Override
    public Ticket save(Ticket ticket) {
        return mapper.toDomain(springData.save(mapper.toJpaEntity(ticket)));
    }
}
