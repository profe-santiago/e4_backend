package com.tickets.event_service.messaging;

import com.tickets.event_service.messaging.event.*;
import com.tickets.event_service.messaging.publisher.StockResultPublisher;
import com.tickets.event_service.tickettype.TicketType;
import com.tickets.event_service.tickettype.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockReservationServiceImpl")
class StockReservationServiceImplTest {

    @Mock private TicketTypeRepository ticketTypeRepository;
    @Mock private StockResultPublisher  stockResultPublisher;

    @InjectMocks
    private StockReservationServiceImpl stockReservationService;

    private UUID orderId;
    private UUID eventId;
    private TicketType ticketType;

    @BeforeEach
    void setUp() {
        orderId  = UUID.randomUUID();
        eventId  = UUID.randomUUID();

        ticketType = new TicketType();
        ticketType.setId(1L);
        ticketType.setName("General");
        ticketType.setPrice(new BigDecimal("80.00"));
        ticketType.setTotalQuantity(100);
        ticketType.setAvailableQuantity(50);
    }

    @Nested
    @DisplayName("reserve — caso exitoso")
    class ReserveSuccess {

        @Test
        @DisplayName("debe decrementar stock y publicar StockReservedEvent")
        void shouldDecrementStock_andPublishReserved() {
            StockReserveCommand command = new StockReserveCommand(
                    orderId, UUID.randomUUID(),
                    List.of(new StockReserveItem(eventId, 1L, 3)));

            given(ticketTypeRepository.findByIdForUpdate(1L)).willReturn(Optional.of(ticketType));
            given(ticketTypeRepository.save(any())).willReturn(ticketType);

            stockReservationService.reserve(command);

            // Verifica decremento
            assertThat(ticketType.getAvailableQuantity()).isEqualTo(47);

            // Verifica publicación de éxito
            ArgumentCaptor<StockReservedEvent> captor = ArgumentCaptor.forClass(StockReservedEvent.class);
            then(stockResultPublisher).should().publishReserved(captor.capture());
            StockReservedEvent published = captor.getValue();
            assertThat(published.getOrderId()).isEqualTo(orderId);
            assertThat(published.getItems()).hasSize(1);
            assertThat(published.getItems().get(0).getUnitPrice()).isEqualByComparingTo("80.00");

            then(stockResultPublisher).should(never()).publishFailed(any());
        }
    }

    @Nested
    @DisplayName("reserve — stock insuficiente")
    class ReserveInsufficientStock {

        @Test
        @DisplayName("debe publicar StockFailedEvent sin modificar nada en DB")
        void shouldPublishFailed_whenInsufficientStock() {
            ticketType.setAvailableQuantity(1); // solo 1 disponible

            StockReserveCommand command = new StockReserveCommand(
                    orderId, UUID.randomUUID(),
                    List.of(new StockReserveItem(eventId, 1L, 5))); // pide 5

            given(ticketTypeRepository.findByIdForUpdate(1L)).willReturn(Optional.of(ticketType));

            stockReservationService.reserve(command);

            // Stock NO se modificó
            assertThat(ticketType.getAvailableQuantity()).isEqualTo(1);

            ArgumentCaptor<StockFailedEvent> captor = ArgumentCaptor.forClass(StockFailedEvent.class);
            then(stockResultPublisher).should().publishFailed(captor.capture());
            assertThat(captor.getValue().getOrderId()).isEqualTo(orderId);
            assertThat(captor.getValue().getReason()).contains("insuficiente");

            then(ticketTypeRepository).should(never()).save(any());
            then(stockResultPublisher).should(never()).publishReserved(any());
        }
    }

    @Nested
    @DisplayName("reserve — ticket type no encontrado")
    class ReserveNotFound {

        @Test
        @DisplayName("debe publicar StockFailedEvent cuando el ticket type no existe")
        void shouldPublishFailed_whenTicketTypeNotFound() {
            StockReserveCommand command = new StockReserveCommand(
                    orderId, UUID.randomUUID(),
                    List.of(new StockReserveItem(eventId, 99L, 1)));

            given(ticketTypeRepository.findByIdForUpdate(99L)).willReturn(Optional.empty());

            stockReservationService.reserve(command);

            then(stockResultPublisher).should().publishFailed(any());
            then(stockResultPublisher).should(never()).publishReserved(any());
            then(ticketTypeRepository).should(never()).save(any());
        }
    }
}
