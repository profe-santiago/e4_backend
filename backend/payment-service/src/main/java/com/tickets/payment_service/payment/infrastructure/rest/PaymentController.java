package com.tickets.payment_service.payment.infrastructure.rest;

import com.tickets.payment_service.payment.application.FindPaymentByIdUseCase;
import com.tickets.payment_service.payment.application.FindPaymentByOrderUseCase;
import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.PaymentId;
import com.tickets.payment_service.payment.infrastructure.rest.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Adaptador de entrada HTTP.
 *
 * Recibe peticiones REST, convierte parámetros a tipos de dominio y delega
 * a los use cases correspondientes. No contiene lógica de negocio.
 */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Consultas de estado de pagos")
public class PaymentController {

    private final FindPaymentByIdUseCase findPaymentByIdUseCase;
    private final FindPaymentByOrderUseCase findPaymentByOrderUseCase;
    private final PaymentRestMapper mapper;

    public PaymentController(FindPaymentByIdUseCase findPaymentByIdUseCase,
                              FindPaymentByOrderUseCase findPaymentByOrderUseCase,
                              PaymentRestMapper mapper) {
        this.findPaymentByIdUseCase = findPaymentByIdUseCase;
        this.findPaymentByOrderUseCase = findPaymentByOrderUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pago por ID")
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(
                findPaymentByIdUseCase.execute(PaymentId.of(id))));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Obtener pago por ID de orden")
    public ResponseEntity<PaymentResponse> getByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(mapper.toResponse(
                findPaymentByOrderUseCase.execute(OrderId.of(orderId))));
    }
}
