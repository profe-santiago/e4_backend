package com.tickets.event_service.messaging;

import com.tickets.event_service.tickettype.application.ReserveStockUseCase;
import com.tickets.event_service.tickettype.domain.Money;
import com.tickets.event_service.tickettype.domain.ReservedStockItem;
import com.tickets.event_service.tickettype.domain.StockEventPublisher;
import com.tickets.event_service.tickettype.domain.TicketType;
import com.tickets.event_service.tickettype.domain.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReserveStockUseCase")
class StockReservationServiceImplTest {

    @Mock private TicketTypeRepository ticketTypeRepository;
    @Mock private StockEventPublisher   stockEventPublisher;

    private ReserveStockUseCase useCase;

    private UUID orderId;
    private UUID eventId;
    private TicketType ticketType;

    @BeforeEach
    void setUp() {
        useCase  = new ReserveStockUseCase(ticketTypeRepository, stockEventPublisher);
        orderId  = UUID.randomUUID();
        eventId  = UUID.randomUUID();

        ticketType = new TicketType();
        ticketType.setId(1L);
        ticketType.setName("General");
        ticketType.setPrice(Money.ofUSD(new BigDecimal("80.00")));
        ticketType.setTotalQuantity(100);
        ticketType.setAvailableQuantity(50);
    }

    @Nested
    @DisplayName("reserve — caso exitoso")
    class ReserveSuccess {

        @Test
        @DisplayName("debe decrementar stock y publicar resultado reservado")
        @SuppressWarnings("unchecked")
        void shouldDecrementStock_andPublishReserved() {
            List<ReserveStockUseCase.ReservationItem> items = List.of(
                    new ReserveStockUseCase.ReservationItem(eventId, 1L, 3));

            given(ticketTypeRepository.findByIdLocked(1L)).willReturn(Optional.of(ticketType));
            given(ticketTypeRepository.save(any())).willReturn(ticketType);

            useCase.execute(orderId, items);

            assertThat(ticketType.getAvailableQuantity()).isEqualTo(47);

            ArgumentCaptor<List<ReservedStockItem>> captor =
                    ArgumentCaptor.forClass((Class<List<ReservedStockItem>>) (Class<?>) List.class);
            then(stockEventPublisher).should().publishReserved(eq(orderId), captor.capture());
            List<ReservedStockItem> published = captor.getValue();
            assertThat(published).hasSize(1);
            assertThat(published.get(0).unitPrice().amount()).isEqualByComparingTo("80.00");

            then(stockEventPublisher).should(never()).publishFailed(any(), any());
        }
    }

    @Nested
    @DisplayName("reserve — stock insuficiente")
    class ReserveInsufficientStock {

        @Test
        @DisplayName("debe publicar fallo sin modificar nada en DB")
        void shouldPublishFailed_whenInsufficientStock() {
            ticketType.setAvailableQuantity(1);

            List<ReserveStockUseCase.ReservationItem> items = List.of(
                    new ReserveStockUseCase.ReservationItem(eventId, 1L, 5));

            given(ticketTypeRepository.findByIdLocked(1L)).willReturn(Optional.of(ticketType));

            useCase.execute(orderId, items);

            assertThat(ticketType.getAvailableQuantity()).isEqualTo(1);

            ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);
            then(stockEventPublisher).should().publishFailed(eq(orderId), reasonCaptor.capture());
            assertThat(reasonCaptor.getValue()).contains("insuficiente");

            then(ticketTypeRepository).should(never()).save(any());
            then(stockEventPublisher).should(never()).publishReserved(any(), any());
        }
    }

    @Nested
    @DisplayName("reserve — ticket type no encontrado")
    class ReserveNotFound {

        @Test
        @DisplayName("debe publicar fallo cuando el ticket type no existe")
        void shouldPublishFailed_whenTicketTypeNotFound() {
            List<ReserveStockUseCase.ReservationItem> items = List.of(
                    new ReserveStockUseCase.ReservationItem(eventId, 99L, 1));

            given(ticketTypeRepository.findByIdLocked(99L)).willReturn(Optional.empty());

            useCase.execute(orderId, items);

            then(stockEventPublisher).should().publishFailed(any(), any());
            then(stockEventPublisher).should(never()).publishReserved(any(), any());
            then(ticketTypeRepository).should(never()).save(any());
        }
    }
}
