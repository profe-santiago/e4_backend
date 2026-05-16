package com.tickets.event_service.event;

import com.tickets.event_service.category.domain.CategoryRepository;
import com.tickets.event_service.event.application.ChangeEventStatusUseCase;
import com.tickets.event_service.event.application.CreateEventUseCase;
import com.tickets.event_service.event.application.DeleteEventUseCase;
import com.tickets.event_service.event.application.GetEventByIdUseCase;
import com.tickets.event_service.event.application.ListPublishedEventsUseCase;
import com.tickets.event_service.event.application.UpdateEventUseCase;
import com.tickets.event_service.event.application.dto.CreateEventCommand;
import com.tickets.event_service.event.application.dto.UpdateEventCommand;
import com.tickets.event_service.event.domain.Event;
import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.event.domain.EventStatus;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.exception.InvalidEventStatusTransitionException;
import com.tickets.event_service.exception.UnauthorizedActionException;
import com.tickets.event_service.shared.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("Event UseCases")
class EventServiceImplTest {

    @Mock private EventRepository eventRepository;
    @Mock private CategoryRepository categoryRepository;

    private UUID organizerId;
    private UUID eventId;
    private Event existingEvent;

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
    }

    @Nested
    @DisplayName("CreateEventUseCase")
    class Create {

        private CreateEventUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new CreateEventUseCase(eventRepository, categoryRepository);
        }

        @Test
        @DisplayName("debe crear el evento con el organizerId indicado")
        void shouldCreate_withOrganizerId() {
            CreateEventCommand command = new CreateEventCommand(
                    organizerId, "Rock en el Parque", null, null,
                    "Estadio Nacional", "Buenos Aires", "Argentina",
                    LocalDateTime.now().plusDays(30), null, null);
            given(eventRepository.save(any(Event.class))).willReturn(existingEvent);

            Event result = useCase.execute(command);

            assertThat(result.getOrganizerId()).isEqualTo(organizerId);
            then(eventRepository).should().save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("GetEventByIdUseCase")
    class FindById {

        private GetEventByIdUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new GetEventByIdUseCase(eventRepository);
        }

        @Test
        @DisplayName("debe retornar el evento cuando existe")
        void shouldReturn_whenFound() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            Event result = useCase.execute(eventId);

            assertThat(result.getId()).isEqualTo(eventId);
            assertThat(result.getTitle()).isEqualTo("Rock en el Parque");
        }

        @Test
        @DisplayName("debe lanzar EventNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            UUID unknownId = UUID.randomUUID();
            given(eventRepository.findById(unknownId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(unknownId))
                    .isInstanceOf(EventNotFoundException.class)
                    .hasMessageContaining(unknownId.toString());
        }
    }

    @Nested
    @DisplayName("ListPublishedEventsUseCase")
    class FindPublished {

        private ListPublishedEventsUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new ListPublishedEventsUseCase(eventRepository);
        }

        @Test
        @DisplayName("debe retornar página de eventos publicados")
        void shouldReturnPage_ofPublishedEvents() {
            PageResult<Event> page = new PageResult<>(List.of(existingEvent), 1, 1, 0, 20);
            given(eventRepository.findPublished(any(EventStatus.class), any(), any(), any(), any(), anyInt(), anyInt()))
                    .willReturn(page);

            PageResult<Event> result = useCase.execute(null, null, null, null, 0, 20);

            assertThat(result.items()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("UpdateEventUseCase")
    class Update {

        private UpdateEventUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new UpdateEventUseCase(eventRepository, categoryRepository);
        }

        @Test
        @DisplayName("debe actualizar cuando el requester es el organizador")
        void shouldUpdate_whenRequesterIsOrganizer() {
            UpdateEventCommand command = new UpdateEventCommand(
                    organizerId, false, "Nuevo Título",
                    null, null, null, null, null, null, null, null);
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));
            given(eventRepository.save(any(Event.class))).willReturn(existingEvent);

            useCase.execute(eventId, command);

            then(eventRepository).should().save(any(Event.class));
        }

        @Test
        @DisplayName("debe actualizar cuando el requester es ADMIN")
        void shouldUpdate_whenRequesterIsAdmin() {
            UpdateEventCommand command = new UpdateEventCommand(
                    UUID.randomUUID(), true, "Nuevo Título",
                    null, null, null, null, null, null, null, null);
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));
            given(eventRepository.save(any(Event.class))).willReturn(existingEvent);

            useCase.execute(eventId, command);

            then(eventRepository).should().save(any(Event.class));
        }

        @Test
        @DisplayName("debe lanzar UnauthorizedActionException cuando el requester no es dueño ni ADMIN")
        void shouldThrow_whenRequesterIsNotOwnerNorAdmin() {
            UpdateEventCommand command = new UpdateEventCommand(
                    UUID.randomUUID(), false, "Nuevo Título",
                    null, null, null, null, null, null, null, null);
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            assertThatThrownBy(() -> useCase.execute(eventId, command))
                    .isInstanceOf(UnauthorizedActionException.class);

            then(eventRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("ChangeEventStatusUseCase")
    class ChangeStatus {

        private ChangeEventStatusUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new ChangeEventStatusUseCase(eventRepository);
        }

        @Test
        @DisplayName("debe cambiar de DRAFT a PUBLISHED cuando la transición es válida")
        void shouldChange_fromDraftToPublished() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));
            given(eventRepository.save(any(Event.class))).willReturn(existingEvent);

            useCase.execute(eventId, EventStatus.PUBLISHED, organizerId, false);

            then(eventRepository).should().save(any(Event.class));
        }

        @Test
        @DisplayName("debe lanzar InvalidEventStatusTransitionException para transición inválida")
        void shouldThrow_whenTransitionIsInvalid() {
            existingEvent.setStatus(EventStatus.CANCELLED);
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            assertThatThrownBy(() -> useCase.execute(eventId, EventStatus.PUBLISHED, organizerId, false))
                    .isInstanceOf(InvalidEventStatusTransitionException.class)
                    .hasMessageContaining("CANCELLED");

            then(eventRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("DeleteEventUseCase")
    class Delete {

        private DeleteEventUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new DeleteEventUseCase(eventRepository);
        }

        @Test
        @DisplayName("debe eliminar el evento cuando el requester es el organizador")
        void shouldDelete_whenOrganizer() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            useCase.execute(eventId, organizerId, false);

            then(eventRepository).should().delete(existingEvent);
        }

        @Test
        @DisplayName("debe lanzar UnauthorizedActionException cuando el requester no tiene permisos")
        void shouldThrow_whenUnauthorized() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            assertThatThrownBy(() -> useCase.execute(eventId, UUID.randomUUID(), false))
                    .isInstanceOf(UnauthorizedActionException.class);

            then(eventRepository).should(never()).delete(any());
        }
    }
}
