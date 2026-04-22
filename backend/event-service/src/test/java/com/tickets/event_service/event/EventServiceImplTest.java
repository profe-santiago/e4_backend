package com.tickets.event_service.event;

import com.tickets.event_service.category.CategoryRepository;
import com.tickets.event_service.event.dto.CreateEventRequest;
import com.tickets.event_service.event.dto.EventResponse;
import com.tickets.event_service.event.dto.UpdateEventRequest;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.exception.InvalidEventStatusTransitionException;
import com.tickets.event_service.exception.UnauthorizedActionException;
import com.tickets.event_service.shared.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventServiceImpl")
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private UUID organizerId;
    private UUID eventId;
    private Event existingEvent;
    private Authentication organizerAuth;
    private Authentication anotherUserAuth;
    private Authentication adminAuth;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        eventId     = UUID.randomUUID();

        existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setOrganizerId(organizerId);
        existingEvent.setTitle("Rock en el Parque");
        existingEvent.setVenue("Estadio Nacional");
        existingEvent.setCity("Buenos Aires");
        existingEvent.setCountry("Argentina");
        existingEvent.setStartDate(LocalDateTime.now().plusDays(30));
        existingEvent.setStatus(EventStatus.DRAFT);

        organizerAuth   = buildAuth(organizerId, "BUYER");
        anotherUserAuth = buildAuth(UUID.randomUUID(), "BUYER");
        adminAuth       = buildAuth(UUID.randomUUID(), "ADMIN");
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("debe crear el evento con el organizerId del JWT")
        void shouldCreate_withOrganizerIdFromJwt() {
            CreateEventRequest request = buildCreateRequest();
            given(eventRepository.save(any(Event.class))).willReturn(existingEvent);

            EventResponse response = eventService.create(request, organizerAuth);

            assertThat(response.getOrganizerId()).isEqualTo(organizerId);
            then(eventRepository).should().save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("debe retornar el evento cuando existe")
        void shouldReturn_whenFound() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            EventResponse response = eventService.findById(eventId);

            assertThat(response.getId()).isEqualTo(eventId);
            assertThat(response.getTitle()).isEqualTo("Rock en el Parque");
        }

        @Test
        @DisplayName("debe lanzar EventNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            UUID unknownId = UUID.randomUUID();
            given(eventRepository.findById(unknownId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.findById(unknownId))
                    .isInstanceOf(EventNotFoundException.class)
                    .hasMessageContaining(unknownId.toString());
        }
    }

    @Nested
    @DisplayName("findPublished")
    class FindPublished {

        @Test
        @DisplayName("debe retornar página de eventos publicados")
        void shouldReturnPage_ofPublishedEvents() {
            Page<Event> page = new PageImpl<>(List.of(existingEvent));
            given(eventRepository.findAllByStatusWithCategory(any(), any(Pageable.class))).willReturn(page);

            PaginatedResponse<EventResponse> response = eventService.findPublished(null, 0, 20);

            assertThat(response.content()).hasSize(1);
            assertThat(response.totalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("debe actualizar cuando el requester es el organizador")
        void shouldUpdate_whenRequesterIsOrganizer() {
            UpdateEventRequest request = new UpdateEventRequest();
            request.setTitle("Nuevo Título");

            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));
            given(eventRepository.save(any(Event.class))).willReturn(existingEvent);

            eventService.update(eventId, request, organizerAuth);

            then(eventRepository).should().save(any(Event.class));
        }

        @Test
        @DisplayName("debe actualizar cuando el requester es ADMIN")
        void shouldUpdate_whenRequesterIsAdmin() {
            UpdateEventRequest request = new UpdateEventRequest();
            request.setTitle("Nuevo Título");

            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));
            given(eventRepository.save(any(Event.class))).willReturn(existingEvent);

            eventService.update(eventId, request, adminAuth);

            then(eventRepository).should().save(any(Event.class));
        }

        @Test
        @DisplayName("debe lanzar UnauthorizedActionException cuando el requester no es dueño ni ADMIN")
        void shouldThrow_whenRequesterIsNotOwnerNorAdmin() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            assertThatThrownBy(() -> eventService.update(eventId, new UpdateEventRequest(), anotherUserAuth))
                    .isInstanceOf(UnauthorizedActionException.class);

            then(eventRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("changeStatus")
    class ChangeStatus {

        @Test
        @DisplayName("debe cambiar de DRAFT a PUBLISHED cuando la transición es válida")
        void shouldChange_fromDraftToPublished() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));
            given(eventRepository.save(any(Event.class))).willReturn(existingEvent);

            eventService.changeStatus(eventId, EventStatus.PUBLISHED, organizerAuth);

            then(eventRepository).should().save(any(Event.class));
        }

        @Test
        @DisplayName("debe lanzar InvalidEventStatusTransitionException para transición inválida")
        void shouldThrow_whenTransitionIsInvalid() {
            existingEvent.setStatus(EventStatus.CANCELLED);
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            assertThatThrownBy(() -> eventService.changeStatus(eventId, EventStatus.PUBLISHED, organizerAuth))
                    .isInstanceOf(InvalidEventStatusTransitionException.class)
                    .hasMessageContaining("CANCELLED");

            then(eventRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("debe eliminar el evento cuando el requester es el organizador")
        void shouldDelete_whenOrganizer() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            eventService.delete(eventId, organizerAuth);

            then(eventRepository).should().delete(existingEvent);
        }

        @Test
        @DisplayName("debe lanzar UnauthorizedActionException cuando el requester no tiene permisos")
        void shouldThrow_whenUnauthorized() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            assertThatThrownBy(() -> eventService.delete(eventId, anotherUserAuth))
                    .isInstanceOf(UnauthorizedActionException.class);

            then(eventRepository).should(never()).delete(any());
        }
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private Authentication buildAuth(UUID userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                userId.toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    private CreateEventRequest buildCreateRequest() {
        CreateEventRequest req = new CreateEventRequest();
        req.setTitle("Rock en el Parque");
        req.setVenue("Estadio Nacional");
        req.setCity("Buenos Aires");
        req.setCountry("Argentina");
        req.setStartDate(LocalDateTime.now().plusDays(30));
        return req;
    }
}
