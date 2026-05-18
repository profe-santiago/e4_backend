package com.tickets.event_service.tickettype;

import com.tickets.event_service.event.domain.Event;
import com.tickets.event_service.event.domain.EventRepository;
import com.tickets.event_service.event.domain.EventStatus;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.exception.TicketTypeNotFoundException;
import com.tickets.event_service.exception.UnauthorizedActionException;
import com.tickets.event_service.tickettype.application.CreateTicketTypeUseCase;
import com.tickets.event_service.tickettype.application.DeleteTicketTypeUseCase;
import com.tickets.event_service.tickettype.application.GetTicketTypeUseCase;
import com.tickets.event_service.tickettype.application.ListTicketTypesUseCase;
import com.tickets.event_service.tickettype.application.dto.CreateTicketTypeCommand;
import com.tickets.event_service.tickettype.domain.Money;
import com.tickets.event_service.tickettype.domain.TicketType;
import com.tickets.event_service.tickettype.domain.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
@DisplayName("TicketType UseCases")
class TicketTypeServiceImplTest {

    @Mock private TicketTypeRepository ticketTypeRepository;
    @Mock private EventRepository eventRepository;

    private UUID organizerId;
    private UUID eventId;
    private Event existingEvent;
    private TicketType existingTicketType;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        eventId     = UUID.randomUUID();

        existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setOrganizerId(organizerId);
        existingEvent.setStatus(EventStatus.DRAFT);

        existingTicketType = TicketType.create(
                eventId, "VIP", null,
                Money.ofUSD(new BigDecimal("150.00")), 100, null, null);
        existingTicketType.setId(1L);
    }

    @Nested
    @DisplayName("CreateTicketTypeUseCase")
    class Create {

        private CreateTicketTypeUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new CreateTicketTypeUseCase(eventRepository, ticketTypeRepository);
        }

        @Test
        @DisplayName("debe crear el tipo de ticket cuando el requester es el organizador")
        void shouldCreate_whenOrganizer() {
            CreateTicketTypeCommand command = new CreateTicketTypeCommand(
                    eventId, organizerId, false,
                    "VIP", null, Money.ofUSD(new BigDecimal("150.00")), 100, null, null);
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));
            given(ticketTypeRepository.save(any(TicketType.class))).willReturn(existingTicketType);

            TicketType result = useCase.execute(command);

            assertThat(result.getName()).isEqualTo("VIP");
            assertThat(result.getAvailableQuantity()).isEqualTo(existingTicketType.getTotalQuantity());
            then(ticketTypeRepository).should().save(any(TicketType.class));
        }

        @Test
        @DisplayName("debe lanzar UnauthorizedActionException cuando el requester no es el organizador")
        void shouldThrow_whenNotOrganizer() {
            CreateTicketTypeCommand command = new CreateTicketTypeCommand(
                    eventId, UUID.randomUUID(), false,
                    "VIP", null, Money.ofUSD(new BigDecimal("150.00")), 100, null, null);
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(UnauthorizedActionException.class);

            then(ticketTypeRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("debe lanzar EventNotFoundException cuando el evento no existe")
        void shouldThrow_whenEventNotFound() {
            UUID unknownId = UUID.randomUUID();
            CreateTicketTypeCommand command = new CreateTicketTypeCommand(
                    unknownId, organizerId, false,
                    "VIP", null, Money.ofUSD(new BigDecimal("150.00")), 100, null, null);
            given(eventRepository.findById(unknownId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(EventNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("ListTicketTypesUseCase")
    class FindAllByEvent {

        private ListTicketTypesUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new ListTicketTypesUseCase(eventRepository, ticketTypeRepository);
        }

        @Test
        @DisplayName("debe retornar los tipos de ticket del evento")
        void shouldReturn_ticketTypes() {
            given(eventRepository.existsById(eventId)).willReturn(true);
            given(ticketTypeRepository.findAllByEventId(eventId)).willReturn(List.of(existingTicketType));

            List<TicketType> result = useCase.execute(eventId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("VIP");
        }

        @Test
        @DisplayName("debe lanzar EventNotFoundException cuando el evento no existe")
        void shouldThrow_whenEventNotFound() {
            given(eventRepository.existsById(eventId)).willReturn(false);

            assertThatThrownBy(() -> useCase.execute(eventId))
                    .isInstanceOf(EventNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("GetTicketTypeUseCase")
    class FindById {

        private GetTicketTypeUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new GetTicketTypeUseCase(ticketTypeRepository);
        }

        @Test
        @DisplayName("debe retornar el tipo de ticket cuando existe")
        void shouldReturn_whenFound() {
            given(ticketTypeRepository.findByIdAndEventId(1L, eventId))
                    .willReturn(Optional.of(existingTicketType));

            TicketType result = useCase.execute(eventId, 1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("debe lanzar TicketTypeNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            given(ticketTypeRepository.findByIdAndEventId(99L, eventId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(eventId, 99L))
                    .isInstanceOf(TicketTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("DeleteTicketTypeUseCase")
    class Delete {

        private DeleteTicketTypeUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new DeleteTicketTypeUseCase(eventRepository, ticketTypeRepository);
        }

        @Test
        @DisplayName("debe eliminar el tipo de ticket cuando el requester es el organizador")
        void shouldDelete_whenOrganizer() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));
            given(ticketTypeRepository.findByIdAndEventId(1L, eventId))
                    .willReturn(Optional.of(existingTicketType));

            useCase.execute(eventId, 1L, organizerId, false);

            then(ticketTypeRepository).should().delete(existingTicketType);
        }

        @Test
        @DisplayName("debe lanzar UnauthorizedActionException cuando no tiene permisos")
        void shouldThrow_whenUnauthorized() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            assertThatThrownBy(() -> useCase.execute(eventId, 1L, UUID.randomUUID(), false))
                    .isInstanceOf(UnauthorizedActionException.class);

            then(ticketTypeRepository).should(never()).delete(any());
        }
    }
}
