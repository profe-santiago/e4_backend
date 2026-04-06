package com.tickets.ticket_service.order;

import com.tickets.ticket_service.exception.InvalidOrderStateException;
import com.tickets.ticket_service.exception.OrderNotFoundException;
import com.tickets.ticket_service.messaging.event.*;
import com.tickets.ticket_service.messaging.publisher.OrderEventPublisher;
import com.tickets.ticket_service.order.dto.CreateOrderRequest;
import com.tickets.ticket_service.order.dto.CreateOrderItemRequest;
import com.tickets.ticket_service.order.dto.OrderResponse;
import com.tickets.ticket_service.shared.PaginatedResponse;
import com.tickets.ticket_service.shared.SecurityUtils;
import com.tickets.ticket_service.ticket.TicketService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;
    private final TicketService ticketService;

    public OrderServiceImpl(OrderRepository orderRepository,
                             OrderEventPublisher eventPublisher,
                             @Lazy TicketService ticketService) {
        this.orderRepository = orderRepository;
        this.eventPublisher  = eventPublisher;
        this.ticketService   = ticketService;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Authentication auth) {
        UUID userId = SecurityUtils.getUserId(auth);

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO); // se actualiza al confirmar
        order.setPaymentMethodId(request.getPaymentMethodId());

        List<OrderItem> items = request.getItems().stream()
                .map(dto -> buildItem(dto, order))
                .toList();

        order.getItems().addAll(items);
        Order saved = orderRepository.save(order);

        // Publica el comando de reserva de stock de forma asíncrona
        StockReserveCommand command = new StockReserveCommand(
                saved.getId(),
                userId,
                request.getItems().stream()
                        .map(i -> new StockReserveItem(i.getEventId(), i.getTicketTypeId(), i.getQuantity()))
                        .toList()
        );
        eventPublisher.publishStockReserve(command);

        return OrderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse confirmOrder(StockReservedEvent event) {
        Order order = findWithItemsOrThrow(event.getOrderId());
        validateTransition(order, OrderStatus.CONFIRMED);

        // Actualiza precios reales devueltos por event-service
        BigDecimal total = BigDecimal.ZERO;
        for (StockReservedItem reservedItem : event.getItems()) {
            for (OrderItem item : order.getItems()) {
                if (item.getTicketTypeId().equals(reservedItem.getTicketTypeId())
                        && item.getEventId().equals(reservedItem.getEventId())) {
                    item.setUnitPrice(reservedItem.getUnitPrice());
                    total = total.add(reservedItem.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);

        // Genera los tickets físicos
        List<OrderConfirmedEvent.ConfirmedTicket> generatedTickets =
                ticketService.generateTickets(saved);

        eventPublisher.publishOrderConfirmed(new OrderConfirmedEvent(
                saved.getId(), saved.getUserId(), saved.getTotalAmount(),
                saved.getPaymentMethodId(), generatedTickets));

        return OrderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse failOrder(UUID orderId, String reason) {
        Order order = findWithItemsOrThrow(orderId);
        validateTransition(order, OrderStatus.FAILED);
        order.setStatus(OrderStatus.FAILED);
        Order saved = orderRepository.save(order);

        eventPublisher.publishOrderCancelled(
                new OrderCancelledEvent(saved.getId(), saved.getUserId(), reason));

        return OrderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID orderId, Authentication auth) {
        Order order = findWithItemsOrThrow(orderId);
        SecurityUtils.verifyOwnerOrAdmin(order.getUserId(), auth);
        validateTransition(order, OrderStatus.CANCELLED);

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        eventPublisher.publishOrderCancelled(
                new OrderCancelledEvent(saved.getId(), saved.getUserId(), "Cancelado por el usuario"));

        return OrderMapper.toResponse(saved);
    }

    @Override
    public OrderResponse findById(UUID id, Authentication auth) {
        Order order = findWithItemsOrThrow(id);
        SecurityUtils.verifyOwnerOrAdmin(order.getUserId(), auth);
        return OrderMapper.toResponse(order);
    }

    @Override
    public PaginatedResponse<OrderResponse> findMyOrders(Authentication auth, int page, int size) {
        UUID userId = SecurityUtils.getUserId(auth);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PaginatedResponse.from(
                orderRepository.findAllByUserId(userId, pageable).map(OrderMapper::toResponse));
    }

    // ── helpers privados ──────────────────────────────────────────────────────

    private Order findWithItemsOrThrow(UUID id) {
        return orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private void validateTransition(Order order, OrderStatus target) {
        if (!order.getStatus().canTransitionTo(target)) {
            throw new InvalidOrderStateException(order.getStatus(), target);
        }
    }

    private OrderItem buildItem(CreateOrderItemRequest dto, Order order) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setEventId(dto.getEventId());
        item.setTicketTypeId(dto.getTicketTypeId());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(BigDecimal.ZERO); // se completa al confirmar
        return item;
    }
}
