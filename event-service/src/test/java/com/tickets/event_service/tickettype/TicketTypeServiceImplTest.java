package com.tickets.event_service.tickettype;

import com.tickets.event_service.event.Event;
import com.tickets.event_service.event.EventRepository;
import com.tickets.event_service.event.EventStatus;
import com.tickets.event_service.exception.EventNotFoundException;
import com.tickets.event_service.exception.TicketTypeNotFoundException;
import com.tickets.event_service.exception.UnauthorizedActionException;
import com.tickets.event_service.tickettype.dto.TicketTypeRequest;
import com.tickets.event_service.tickettype.dto.TicketTypeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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
@DisplayName("TicketTypeServiceImpl")
class TicketTypeServiceImplTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private TicketTypeServiceImpl ticketTypeService;

    private UUID organizerId;
    private UUID eventId;
    private Event existingEvent;
    private TicketType existingTicketType;
    private Authentication organizerAuth;
    private Authentication anotherUserAuth;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        eventId     = UUID.randomUUID();

        existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setOrganizerId(organizerId);
        existingEvent.setStatus(EventStatus.DRAFT);

        existingTicketType = new TicketType();
        existingTicketType.setId(1L);
        existingTicketType.setEvent(existingEvent);
        existingTicketType.setName("VIP");
        existingTicketType.setPrice(new BigDecimal("150.00"));
        existingTicketType.setTotalQuantity(100);
        existingTicketType.setAvailableQuantity(100);

        organizerAuth   = buildAuth(organizerId, "BUYER");
        anotherUserAuth = buildAuth(UUID.randomUUID(), "BUYER");
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("debe crear el tipo de ticket cuando el requester es el organizador")
        void shouldCreate_whenOrganizer() {
            TicketTypeRequest request = buildRequest();
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));
            given(ticketTypeRepository.save(any(TicketType.class))).willReturn(existingTicketType);

            TicketTypeResponse response = ticketTypeService.create(eventId, request, organizerAuth);

            assertThat(response.getName()).isEqualTo("VIP");
            assertThat(response.getAvailableQuantity()).isEqualTo(existingTicketType.getTotalQuantity());
            then(ticketTypeRepository).should().save(any(TicketType.class));
        }

        @Test
        @DisplayName("debe lanzar UnauthorizedActionException cuando el requester no es el organizador")
        void shouldThrow_whenNotOrganizer() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            assertThatThrownBy(() -> ticketTypeService.create(eventId, buildRequest(), anotherUserAuth))
                    .isInstanceOf(UnauthorizedActionException.class);

            then(ticketTypeRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("debe lanzar EventNotFoundException cuando el evento no existe")
        void shouldThrow_whenEventNotFound() {
            UUID unknownId = UUID.randomUUID();
            given(eventRepository.findById(unknownId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> ticketTypeService.create(unknownId, buildRequest(), organizerAuth))
                    .isInstanceOf(EventNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAllByEvent")
    class FindAllByEvent {

        @Test
        @DisplayName("debe retornar los tipos de ticket del evento")
        void shouldReturn_ticketTypes() {
            given(eventRepository.existsById(eventId)).willReturn(true);
            given(ticketTypeRepository.findAllByEventId(eventId)).willReturn(List.of(existingTicketType));

            List<TicketTypeResponse> result = ticketTypeService.findAllByEvent(eventId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("VIP");
        }

        @Test
        @DisplayName("debe lanzar EventNotFoundException cuando el evento no existe")
        void shouldThrow_whenEventNotFound() {
            given(eventRepository.existsById(eventId)).willReturn(false);

            assertThatThrownBy(() -> ticketTypeService.findAllByEvent(eventId))
                    .isInstanceOf(EventNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("debe retornar el tipo de ticket cuando existe")
        void shouldReturn_whenFound() {
            given(ticketTypeRepository.findByIdAndEventId(1L, eventId))
                    .willReturn(Optional.of(existingTicketType));

            TicketTypeResponse response = ticketTypeService.findById(eventId, 1L);

            assertThat(response.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("debe lanzar TicketTypeNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            given(ticketTypeRepository.findByIdAndEventId(99L, eventId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> ticketTypeService.findById(eventId, 99L))
                    .isInstanceOf(TicketTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("debe eliminar el tipo de ticket cuando el requester es el organizador")
        void shouldDelete_whenOrganizer() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));
            given(ticketTypeRepository.findByIdAndEventId(1L, eventId))
                    .willReturn(Optional.of(existingTicketType));

            ticketTypeService.delete(eventId, 1L, organizerAuth);

            then(ticketTypeRepository).should().delete(existingTicketType);
        }

        @Test
        @DisplayName("debe lanzar UnauthorizedActionException cuando no tiene permisos")
        void shouldThrow_whenUnauthorized() {
            given(eventRepository.findById(eventId)).willReturn(Optional.of(existingEvent));

            assertThatThrownBy(() -> ticketTypeService.delete(eventId, 1L, anotherUserAuth))
                    .isInstanceOf(UnauthorizedActionException.class);

            then(ticketTypeRepository).should(never()).delete(any());
        }
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private Authentication buildAuth(UUID userId, String role) {
        return new UsernamePasswordAuthenticationToken(
                userId.toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    private TicketTypeRequest buildRequest() {
        TicketTypeRequest req = new TicketTypeRequest();
        req.setName("VIP");
        req.setPrice(new BigDecimal("150.00"));
        req.setTotalQuantity(100);
        return req;
    }
}
