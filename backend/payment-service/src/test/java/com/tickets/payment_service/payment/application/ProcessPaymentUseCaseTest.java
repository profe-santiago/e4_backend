package com.tickets.payment_service.payment.application;

import com.tickets.payment_service.payment.application.dto.ProcessPaymentCommand;
import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.Payment;
import com.tickets.payment_service.payment.domain.PaymentChargeResult;
import com.tickets.payment_service.payment.domain.PaymentStatus;
import com.tickets.payment_service.payment.domain.UserId;
import com.tickets.payment_service.payment.domain.port.PaymentEventPort;
import com.tickets.payment_service.payment.domain.port.PaymentGateway;
import com.tickets.payment_service.payment.domain.port.PaymentRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessPaymentUseCase")
class ProcessPaymentUseCaseTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private PaymentGateway paymentGateway;
    @Mock private PaymentEventPort paymentEventPort;

    @InjectMocks
    private ProcessPaymentUseCase useCase;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID USER_ID  = UUID.randomUUID();
    private static final String PI_ID  = "pi_test_visa";

    private ProcessPaymentCommand buildCommand() {
        return new ProcessPaymentCommand(ORDER_ID, USER_ID, new BigDecimal("500.00"), "USD", PI_ID);
    }

    private Payment pendingPaymentStub() {
        return Payment.create(
                OrderId.of(ORDER_ID),
                UserId.of(USER_ID),
                com.tickets.payment_service.payment.domain.Money.of(new BigDecimal("500.00"), "USD"),
                PI_ID
        );
    }

    @BeforeEach
    void stubSave() {
        given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("execute — orden nueva")
    class NuevaOrden {

        @Test
        @DisplayName("Stripe exitoso → pago APPROVED + publica PaymentCompletedEvent")
        void whenStripeSucceeds_shouldApproveAndPublishCompleted() {
            given(paymentRepository.existsByOrderId(OrderId.of(ORDER_ID))).willReturn(false);
            given(paymentGateway.charge(any(), any(), any()))
                    .willReturn(PaymentChargeResult.success("pi_test_123"));

            useCase.execute(buildCommand());

            // Se persiste dos veces: PENDING y luego APPROVED
            then(paymentRepository).should(org.mockito.Mockito.times(2)).save(any(Payment.class));

            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            then(paymentEventPort).should().publishPaymentCompleted(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(captor.getValue().getTransactionId()).isEqualTo("pi_test_123");

            then(paymentEventPort).should(never()).publishPaymentFailed(any(), any(), any());
        }

        @Test
        @DisplayName("Gateway rechaza → pago REJECTED + publica PaymentFailedEvent")
        void whenGatewayFails_shouldRejectAndPublishFailed() {
            given(paymentRepository.existsByOrderId(OrderId.of(ORDER_ID))).willReturn(false);
            given(paymentGateway.charge(any(), any(), any()))
                    .willReturn(PaymentChargeResult.failure("Tu tarjeta fue rechazada."));

            useCase.execute(buildCommand());

            then(paymentRepository).should(org.mockito.Mockito.times(2)).save(any(Payment.class));

            ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);
            then(paymentEventPort).should()
                    .publishPaymentFailed(any(OrderId.class), any(UserId.class), reasonCaptor.capture());
            assertThat(reasonCaptor.getValue()).contains("rechazada");

            then(paymentEventPort).should(never()).publishPaymentCompleted(any());
        }

        @Test
        @DisplayName("Estado inesperado de gateway → REJECTED + publica PaymentFailedEvent")
        void whenGatewayReturnsUnexpectedStatus_shouldRejectAndPublishFailed() {
            given(paymentRepository.existsByOrderId(OrderId.of(ORDER_ID))).willReturn(false);
            given(paymentGateway.charge(any(), any(), any()))
                    .willReturn(PaymentChargeResult.failure("Unexpected payment status: requires_action"));

            useCase.execute(buildCommand());

            ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);
            then(paymentEventPort).should()
                    .publishPaymentFailed(any(), any(), reasonCaptor.capture());
            assertThat(reasonCaptor.getValue()).contains("requires_action");
        }
    }

    @Nested
    @DisplayName("execute — idempotencia")
    class Idempotencia {

        @Test
        @DisplayName("orden duplicada → no llama al gateway ni publica eventos")
        void whenDuplicateOrderId_shouldBeIdempotentAndSkipGateway() {
            given(paymentRepository.existsByOrderId(OrderId.of(ORDER_ID))).willReturn(true);

            useCase.execute(buildCommand());

            then(paymentGateway).shouldHaveNoInteractions();
            then(paymentRepository).should(never()).save(any());
            then(paymentEventPort).shouldHaveNoInteractions();
        }
    }
}
