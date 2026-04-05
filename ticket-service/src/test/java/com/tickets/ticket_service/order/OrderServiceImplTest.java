package com.tickets.ticket_service.order;

import com.tickets.ticket_service.exception.InvalidOrderStateException;
import com.tickets.ticket_service.exception.OrderNotFoundException;
import com.tickets.ticket_service.exception.UnauthorizedActionException;
import com.tickets.ticket_service.messaging.event.StockReservedEvent;
import com.tickets.ticket_service.messaging.event.StockReservedItem;
import com.tickets.ticket_service.messaging.publisher.OrderEventPublisher;
import com.tickets.ticket_service.order.dto.CreateOrderItemRequest;
import com.tickets.ticket_service.order.dto.CreateOrderRequest;
import com.tickets.ticket_service.order.dto.OrderResponse;
import com.tickets.ticket_service.ticket.TicketService;
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
@DisplayName("OrderServiceImpl")
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderEventPublisher eventPublisher;
    @Mock private TicketService ticketService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID userId;
    private UUID orderId;
    private Order pendingOrder;
    private Authentication ownerAuth;
    private Authentication otherAuth;

    @BeforeEach
    void setUp() {
        userId  = UUID.randomUUID();
        orderId = UUID.randomUUID();

        OrderItem item = new OrderItem();
        item.setEventId(UUID.randomUUID());
        item.setTicketTypeId(1L);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.ZERO);

        pendingOrder = new Order();
        pendingOrder.setId(orderId);
        pendingOrder.setUserId(userId);
        pendingOrder.setStatus(OrderStatus.PENDING);
        pendingOrder.setTotalAmount(BigDecimal.ZERO);
        pendingOrder.getItems().add(item);
        item.setOrder(pendingOrder);

        ownerAuth = auth(userId, "BUYER");
        otherAuth = auth(UUID.randomUUID(), "BUYER");
    }

    @Nested
    @DisplayName("createOrder")
    class CreateOrder {

        @Test
        @DisplayName("debe crear orden PENDING y publicar StockReserveCommand")
        void shouldCreatePendingOrder_andPublishCommand() {
            given(orderRepository.save(any(Order.class))).willReturn(pendingOrder);

            CreateOrderRequest request = buildCreateRequest();
            OrderResponse response = orderService.createOrder(request, ownerAuth);

            assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
            then(eventPublisher).should().publishStockReserve(any());
        }
    }

    @Nested
    @DisplayName("confirmOrder")
    class ConfirmOrder {

        @Test
        @DisplayName("debe confirmar la orden, actualizar precios y generar tickets")
        void shouldConfirmOrder_updatePrices_generateTickets() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));
            given(orderRepository.save(any(Order.class))).willReturn(pendingOrder);
            given(ticketService.generateTickets(any(Order.class))).willReturn(List.of());

            StockReservedEvent event = new StockReservedEvent(orderId, List.of(
                    new StockReservedItem(
                            pendingOrder.getItems().get(0).getEventId(),
                            1L, 2, new BigDecimal("150.00"))));

            OrderResponse response = orderService.confirmOrder(event);

            assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            then(ticketService).should().generateTickets(any(Order.class));
            then(eventPublisher).should().publishOrderConfirmed(any());
        }

        @Test
        @DisplayName("debe lanzar InvalidOrderStateException si la orden ya está confirmada")
        void shouldThrow_whenOrderAlreadyConfirmed() {
            pendingOrder.setStatus(OrderStatus.CONFIRMED);
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));

            StockReservedEvent event = new StockReservedEvent(orderId, List.of());

            assertThatThrownBy(() -> orderService.confirmOrder(event))
                    .isInstanceOf(InvalidOrderStateException.class);

            then(ticketService).should(never()).generateTickets(any());
        }
    }

    @Nested
    @DisplayName("failOrder")
    class FailOrder {

        @Test
        @DisplayName("debe marcar la orden como FAILED y publicar cancelación")
        void shouldFailOrder_andPublishCancelled() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));
            given(orderRepository.save(any(Order.class))).willReturn(pendingOrder);

            orderService.failOrder(orderId, "Sin stock");

            then(eventPublisher).should().publishOrderCancelled(any());
        }
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {

        @Test
        @DisplayName("debe cancelar la orden cuando el requester es el dueño")
        void shouldCancel_whenOwner() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));
            given(orderRepository.save(any(Order.class))).willReturn(pendingOrder);

            orderService.cancelOrder(orderId, ownerAuth);

            then(eventPublisher).should().publishOrderCancelled(any());
        }

        @Test
        @DisplayName("debe lanzar UnauthorizedActionException si el requester no es el dueño")
        void shouldThrow_whenNotOwner() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(orderId, otherAuth))
                    .isInstanceOf(UnauthorizedActionException.class);

            then(orderRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("debe lanzar InvalidOrderStateException si la orden no está en PENDING")
        void shouldThrow_whenNotPending() {
            pendingOrder.setStatus(OrderStatus.CONFIRMED);
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(orderId, ownerAuth))
                    .isInstanceOf(InvalidOrderStateException.class);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("debe retornar la orden cuando el dueño la consulta")
        void shouldReturn_whenOwner() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.of(pendingOrder));

            OrderResponse response = orderService.findById(orderId, ownerAuth);

            assertThat(response.getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("debe lanzar OrderNotFoundException cuando no existe")
        void shouldThrow_whenNotFound() {
            given(orderRepository.findByIdWithItems(orderId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findById(orderId, ownerAuth))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Authentication auth(UUID id, String role) {
        return new UsernamePasswordAuthenticationToken(
                id.toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    private CreateOrderRequest buildCreateRequest() {
        CreateOrderItemRequest item = new CreateOrderItemRequest();
        item.setEventId(UUID.randomUUID());
        item.setTicketTypeId(1L);
        item.setQuantity(2);

        CreateOrderRequest req = new CreateOrderRequest();
        req.setItems(List.of(item));
        return req;
    }
}
