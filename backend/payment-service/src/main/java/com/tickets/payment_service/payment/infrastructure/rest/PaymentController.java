package com.tickets.payment_service.payment.infrastructure.rest;

import com.tickets.payment_service.payment.application.CreatePaymentIntentUseCase;
import com.tickets.payment_service.payment.application.FindPaymentByIdUseCase;
import com.tickets.payment_service.payment.application.FindPaymentByOrderUseCase;
import com.tickets.payment_service.payment.domain.Money;
import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.PaymentId;
import com.tickets.payment_service.payment.infrastructure.rest.dto.CreatePaymentIntentRequest;
import com.tickets.payment_service.payment.infrastructure.rest.dto.CreatePaymentIntentResponse;
import com.tickets.payment_service.payment.infrastructure.rest.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Pagos y consultas de estado")
public class PaymentController {

    private final CreatePaymentIntentUseCase createPaymentIntent;
    private final FindPaymentByIdUseCase findPaymentByIdUseCase;
    private final FindPaymentByOrderUseCase findPaymentByOrderUseCase;
    private final PaymentRestMapper mapper;

    public PaymentController(CreatePaymentIntentUseCase createPaymentIntent,
                              FindPaymentByIdUseCase findPaymentByIdUseCase,
                              FindPaymentByOrderUseCase findPaymentByOrderUseCase,
                              PaymentRestMapper mapper) {
        this.createPaymentIntent = createPaymentIntent;
        this.findPaymentByIdUseCase = findPaymentByIdUseCase;
        this.findPaymentByOrderUseCase = findPaymentByOrderUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/intent")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear PaymentIntent — el frontend lo confirma con 3DS si es necesario")
    public CreatePaymentIntentResponse createIntent(@Valid @RequestBody CreatePaymentIntentRequest request) {
        var result = createPaymentIntent.execute(
                Money.of(request.amount(), request.currency()));
        return new CreatePaymentIntentResponse(result.clientSecret(), result.paymentIntentId());
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
