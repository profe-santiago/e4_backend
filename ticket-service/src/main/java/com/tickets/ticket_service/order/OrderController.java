package com.tickets.ticket_service.order;

import com.tickets.ticket_service.order.dto.CreateOrderRequest;
import com.tickets.ticket_service.order.dto.OrderResponse;
import com.tickets.ticket_service.shared.PaginatedResponse;
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

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear orden de compra (inicia flujo asíncrono de reserva)")
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request, Authentication auth) {
        return orderService.createOrder(request, auth);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden por ID")
    public OrderResponse findById(@PathVariable UUID id, Authentication auth) {
        return orderService.findById(id, auth);
    }

    @GetMapping("/my")
    @Operation(summary = "Mis órdenes (paginado)")
    public PaginatedResponse<OrderResponse> findMyOrders(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return orderService.findMyOrders(auth, page, size);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar orden (solo si está en PENDING)")
    public OrderResponse cancel(@PathVariable UUID id, Authentication auth) {
        return orderService.cancelOrder(id, auth);
    }
}
