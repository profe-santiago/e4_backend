package com.tickets.ticket_service.order;

import com.tickets.ticket_service.exception.InvalidOrderStateException;
import com.tickets.ticket_service.exception.OrderNotFoundException;
import com.tickets.ticket_service.exception.UnauthorizedActionException;
import com.tickets.ticket_service.order.application.CancelOrderUseCase;
import com.tickets.ticket_service.order.application.ConfirmOrderUseCase;
import com.tickets.ticket_service.order.application.CreateOrderUseCase;
import com.tickets.ticket_service.order.application.FailOrderUseCase;
import com.tickets.ticket_service.order.application.GetOrderByIdUseCase;
import com.tickets.ticket_service.order.application.dto.CreateOrderCommand;
import com.tickets.ticket_service.order.domain.Order;
import com.tickets.ticket_service.order.domain.OrderEventPublisher;
import com.tickets.ticket_service.order.domain.OrderItem;
import com.tickets.ticket_service.order.domain.OrderRepository;
import com.tickets.ticket_service.order.domain.OrderStatus;
import com.tickets.ticket_service.order.domain.StockConfirmationItem;
import com.tickets.ticket_service.ticket.application.GenerateTicketsUseCase;
import com.tickets.ticket_service.ticket.application.dto.GeneratedTicketData;
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
@DisplayName("Order UseCases")
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderEventPublisher eventPublisher;
    @Mock private GenerateTicketsUseCase generateTickets;

    private UUID userId;
    private UUID orderId;
    private UUID eventId;
    private Order pendingOrder;

    @BeforeEach
    void setUp() {
        userId  = UUID.randomUUID();
        orderId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        OrderItem item = OrderItem.create(eventId, 1L, 2);
        item.setId(1L);

        pendingOrder = Order.create(userId, \"pi_test\", List.of(item));
        pendingOrder.setId(orderId);
    }

    // ── CreateOrderUseCase ────────────────────────────────────────────────────

    @Nested
    @DisplayName("CreateOrderUseCase")
    class CreateOrderTests {

        private CreateOrderUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new CreateOrderUseCase(orderRepository, eventPublisher);
        }

        @Test
        @DisplayName("debe crear orden PENDING y publicar StockReserveCommand")
        void shouldCreatePendingOrder_andPublishCommand() {
            given(orderRepository.save(any(Order.class))).willReturn(pendingOrder);

            CreateOrderCommand command = new CreateOrderCommand(userId, \"pi_test\", List.of(
                    new CreateOrderCommand.OrderItemData(eventId, 1L, 2)));

            Order result = useCase.execute(command);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
            then(eventPublisher).should().publishStockReserve(any(), any(), any());
        }
    }

    // ── ConfirmOrderUseCase ───────────────────────────────────────────────────

    @Nested
    @DisplayName("ConfirmOrderUseCase")
    class ConfirmOrderTests {

        private ConfirmOrderUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new ConfirmOrderUseCase(orderRepository, generateTickets, eventPublisher);
        }

        @Test
        @DisplayName("debe confirmar la orden, actualizar precios y generar tickets")
        void shouldConfirmOrder_updatePrices_generateTickets() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));
            given(orderRepository.save(any(Order.class))).willReturn(pendingOrder);
            given(generateTickets.execute(any())).willReturn(List.of(
                    new GeneratedTicketData(UUID.randomUUID(), eventId, 1L, "QR123")));

            List<StockConfirmationItem> reserved = List.of(
                    new StockConfirmationItem(eventId, 1L, 2, new BigDecimal("150.00")));

            Order result = useCase.execute(orderId, reserved);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            then(generateTickets).should().execute(any());
            then(eventPublisher).should().publishOrderConfirmed(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("debe lanzar InvalidOrderStateException si la orden ya está confirmada")
        void shouldThrow_whenOrderAlreadyConfirmed() {
            pendingOrder.setStatus(OrderStatus.CONFIRMED);
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> useCase.execute(orderId, List.of()))
                    .isInstanceOf(InvalidOrderStateException.class);

            then(generateTickets).should(never()).execute(any());
        }

        @Test
        @DisplayName("debe lanzar OrderNotFoundException si la orden no existe")
        void shouldThrow_whenOrderNotFound() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(orderId, List.of()))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    // ── FailOrderUseCase ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("FailOrderUseCase")
    class FailOrderTests {

        private FailOrderUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new FailOrderUseCase(orderRepository, eventPublisher);
        }

        @Test
        @DisplayName("debe marcar la orden como FAILED y publicar cancelación")
        void shouldFailOrder_andPublishCancelled() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));
            given(orderRepository.save(any(Order.class))).willReturn(pendingOrder);

            useCase.execute(orderId, "Sin stock");

            then(eventPublisher).should().publishOrderCancelled(any(), any(), any());
        }
    }

    // ── CancelOrderUseCase ────────────────────────────────────────────────────

    @Nested
    @DisplayName("CancelOrderUseCase")
    class CancelOrderTests {

        private CancelOrderUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new CancelOrderUseCase(orderRepository, eventPublisher);
        }

        @Test
        @DisplayName("debe cancelar la orden cuando el requester es el dueño")
        void shouldCancel_whenOwner() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));
            given(orderRepository.save(any(Order.class))).willReturn(pendingOrder);

            Order result = useCase.execute(orderId, userId, false);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            then(eventPublisher).should().publishOrderCancelled(any(), any(), any());
        }

        @Test
        @DisplayName("debe cancelar cuando el requester es admin aunque no sea el dueño")
        void shouldCancel_whenAdmin() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));
            given(orderRepository.save(any(Order.class))).willReturn(pendingOrder);

            Order result = useCase.execute(orderId, UUID.randomUUID(), true);

            assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("debe lanzar UnauthorizedActionException si el requester no es el dueño")
        void shouldThrow_whenNotOwner() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> useCase.execute(orderId, UUID.randomUUID(), false))
                    .isInstanceOf(UnauthorizedActionException.class);

            then(orderRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("debe lanzar InvalidOrderStateException si la orden no está en PENDING")
        void shouldThrow_whenNotPending() {
            pendingOrder.setStatus(OrderStatus.CONFIRMED);
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> useCase.execute(orderId, userId, false))
                    .isInstanceOf(InvalidOrderStateException.class);
        }
    }

    // ── GetOrderByIdUseCase ───────────────────────────────────────────────────

    @Nested
    @DisplayName("GetOrderByIdUseCase")
    class GetOrderByIdTests {

        private GetOrderByIdUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new GetOrderByIdUseCase(orderRepository);
        }

        @Test
        @DisplayName("debe retornar la orden cuando el dueño la consulta")
        void shouldReturn_whenOwner() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));

            Order result = useCase.execute(orderId, userId, false);

            assertThat(result.getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("debe lanzar OrderNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(orderId, userId, false))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("debe lanzar UnauthorizedActionException si no es el dueño ni admin")
        void shouldThrow_whenNotOwnerOrAdmin() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> useCase.execute(orderId, UUID.randomUUID(), false))
                    .isInstanceOf(UnauthorizedActionException.class);
        }
    }
}
