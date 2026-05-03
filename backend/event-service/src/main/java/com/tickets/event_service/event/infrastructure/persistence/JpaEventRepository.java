package com.tickets.event_service.event.infrastructure.persistence;

import com.tickets.event_service.event.domain.Event;
import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.event.domain.EventStatus;
import com.tickets.event_service.shared.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia — implementa el puerto EventRepository del dominio
 * usando Spring Data JPA internamente.
 *
 * Spring Data Page y Pageable nunca cruzan al dominio.
 */
@Repository
@Transactional(readOnly = true)
public class JpaEventRepository implements EventRepository {

    private final SpringDataEventRepository springData;
    private final EventPersistenceMapper mapper;

    public JpaEventRepository(SpringDataEventRepository springData,
                               EventPersistenceMapper mapper) {
        this.springData = springData;
        this.mapper = mapper;
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return springData.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return springData.existsById(id);
    }

    @Override
    public PageResult<Event> findPublished(EventStatus status, Long categoryId, String search, String city, String venue, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());

        String normalizedSearch = (search != null && !search.isBlank()) ? search.trim() : "";
        String normalizedCity   = (city   != null && !city.isBlank())   ? city.trim()   : "";
        String normalizedVenue  = (venue  != null && !venue.isBlank())  ? venue.trim()  : "";

        Page<EventJpaEntity> result = springData.findAllByFilters(
                status, categoryId, normalizedSearch, normalizedCity, normalizedVenue, pageable);

        List<Event> items = result.getContent().stream()
                .map(mapper::toDomain)
                .toList();

        return new PageResult<>(items, result.getTotalElements(),
                result.getTotalPages(), result.getNumber(), result.getSize());
    }

    @Override
    public List<Event> findAllByOrganizerId(UUID organizerId) {
        return springData.findAllByOrganizerId(organizerId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Event save(Event event) {
        EventJpaEntity entity = mapper.toJpaEntity(event);
        return mapper.toDomain(springData.save(entity));
    }

    @Override
    @Transactional
    public void delete(Event event) {
        springData.findById(event.getId()).ifPresent(springData::delete);
    }
}
