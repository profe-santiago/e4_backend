package com.tickets.ticket_service.order.infrastructure.rest;

import com.tickets.ticket_service.order.application.CancelOrderUseCase;
import com.tickets.ticket_service.order.application.CreateOrderUseCase;
import com.tickets.ticket_service.order.application.GetOrderByIdUseCase;
import com.tickets.ticket_service.order.application.ListMyOrdersUseCase;
import com.tickets.ticket_service.order.application.RequestRefundUseCase;
import com.tickets.ticket_service.order.infrastructure.rest.dto.CreateOrderRequest;
import com.tickets.ticket_service.order.infrastructure.rest.dto.OrderResponse;
import com.tickets.ticket_service.shared.PaginatedResponse;
import com.tickets.ticket_service.shared.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final CreateOrderUseCase createOrder;
    private final GetOrderByIdUseCase getOrderById;
    private final ListMyOrdersUseCase listMyOrders;
    private final CancelOrderUseCase cancelOrder;
    private final RequestRefundUseCase requestRefund;
    private final OrderRestMapper mapper;

    public OrderController(CreateOrderUseCase createOrder,
                            GetOrderByIdUseCase getOrderById,
                            ListMyOrdersUseCase listMyOrders,
                            CancelOrderUseCase cancelOrder,
                            RequestRefundUseCase requestRefund,
                            OrderRestMapper mapper) {
        this.createOrder = createOrder;
        this.getOrderById = getOrderById;
        this.listMyOrders = listMyOrders;
        this.cancelOrder = cancelOrder;
        this.requestRefund = requestRefund;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear orden de compra (inicia flujo asíncrono de reserva)")
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request,
                                 Authentication auth) {
        UUID userId = SecurityUtils.getUserId(auth);
        return mapper.toResponse(createOrder.execute(mapper.toCommand(request, userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden por ID")
    public OrderResponse findById(@PathVariable UUID id, Authentication auth) {
        UUID userId = SecurityUtils.getUserId(auth);
        boolean isAdmin = SecurityUtils.isAdmin(auth);
        return mapper.toResponse(getOrderById.execute(id, userId, isAdmin));
    }

    @GetMapping("/my")
    @Operation(summary = "Mis órdenes (paginado)")
    public PaginatedResponse<OrderResponse> findMyOrders(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID userId = SecurityUtils.getUserId(auth);
        return PaginatedResponse.from(listMyOrders.execute(userId, page, size)
                .map(mapper::toResponse));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar orden (solo si está en PENDING)")
    public OrderResponse cancel(@PathVariable UUID id, Authentication auth) {
        UUID userId = SecurityUtils.getUserId(auth);
        boolean isAdmin = SecurityUtils.isAdmin(auth);
        return mapper.toResponse(cancelOrder.execute(id, userId, isAdmin));
    }

    @PatchMapping("/{id}/refund")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Solicitar reembolso (solo si está en CONFIRMED) — proceso asíncrono")
    public OrderResponse refund(@PathVariable UUID id, Authentication auth) {
        UUID userId = SecurityUtils.getUserId(auth);
        boolean isAdmin = SecurityUtils.isAdmin(auth);
        return mapper.toResponse(requestRefund.execute(id, userId, isAdmin));
    }
}
