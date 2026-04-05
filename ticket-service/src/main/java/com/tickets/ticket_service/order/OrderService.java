package com.tickets.ticket_service.order;

import com.tickets.ticket_service.messaging.event.StockReservedEvent;
import com.tickets.ticket_service.order.dto.CreateOrderRequest;
import com.tickets.ticket_service.order.dto.OrderResponse;
import com.tickets.ticket_service.shared.PaginatedResponse;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface OrderService {

    /** Crea la orden en PENDING y publica StockReserveCommand */
    OrderResponse createOrder(CreateOrderRequest request, Authentication auth);

    /** Llamado por el consumer cuando event-service confirma el stock */
    OrderResponse confirmOrder(StockReservedEvent event);

    /** Llamado por el consumer cuando event-service rechaza el stock */
    OrderResponse failOrder(UUID orderId, String reason);

    /** Cancelación manual iniciada por el usuario */
    OrderResponse cancelOrder(UUID orderId, Authentication auth);

    OrderResponse findById(UUID id, Authentication auth);

    PaginatedResponse<OrderResponse> findMyOrders(Authentication auth, int page, int size);
}
