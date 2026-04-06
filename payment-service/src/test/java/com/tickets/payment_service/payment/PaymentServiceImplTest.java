package com.tickets.payment_service.payment;

import com.stripe.exception.CardException;
import com.stripe.model.PaymentIntent;
import com.tickets.payment_service.exception.PaymentNotFoundException;
import com.tickets.payment_service.messaging.event.OrderConfirmedEvent;
import com.tickets.payment_service.messaging.event.PaymentCompletedEvent;
import com.tickets.payment_service.messaging.event.PaymentFailedEvent;
import com.tickets.payment_service.messaging.publisher.PaymentEventPublisher;
import com.tickets.payment_service.payment.gateway.StripeGateway;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl")
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private StripeGateway stripeGateway;
    @Mock private PaymentEventPublisher eventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private static final UUID ORDER_ID  = UUID.randomUUID();
    private static final UUID USER_ID   = UUID.randomUUID();
    private static final String PM_ID   = "pm_test_visa";
    private static final BigDecimal AMOUNT = new BigDecimal("500.00");

    private OrderConfirmedEvent buildEvent() {
        return new OrderConfirmedEvent(ORDER_ID, USER_ID, AMOUNT, PM_ID, List.of());
    }

    @Nested
    @DisplayName("processPayment")
    class ProcessPayment {

        @Test
        @DisplayName("dado orden nueva y Stripe exitoso → guarda pago APPROVED y publica PaymentCompletedEvent")
        void whenNewOrderAndStripeSucceeds_shouldApproveAndPublishCompleted() throws Exception {
            // arrange
            given(paymentRepository.existsByOrderId(ORDER_ID)).willReturn(false);

            Payment savedPayment = new Payment();
            savedPayment.setId(UUID.randomUUID());
            savedPayment.setOrderId(ORDER_ID);
            savedPayment.setUserId(USER_ID);
            savedPayment.setAmount(AMOUNT);
            savedPayment.setStatus(PaymentStatus.PENDING);
            given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);

            PaymentIntent mockIntent = mock(PaymentIntent.class);
            given(mockIntent.getStatus()).willReturn("succeeded");
            given(mockIntent.getId()).willReturn("pi_test_123");
            given(mockIntent.getPaymentMethodTypes()).willReturn(List.of("card"));
            given(stripeGateway.charge(AMOUNT, "MXN", PM_ID, ORDER_ID)).willReturn(mockIntent);

            // act
            paymentService.processPayment(buildEvent());

            // assert — se guardó dos veces: PENDING y luego APPROVED
            then(paymentRepository).should(times(2)).save(any(Payment.class));

            ArgumentCaptor<PaymentCompletedEvent> captor = ArgumentCaptor.forClass(PaymentCompletedEvent.class);
            then(eventPublisher).should().publishPaymentCompleted(captor.capture());
            assertThat(captor.getValue().getOrderId()).isEqualTo(ORDER_ID);
            assertThat(captor.getValue().getStripePaymentIntentId()).isEqualTo("pi_test_123");

            then(eventPublisher).should(never()).publishPaymentFailed(any());
        }

        @Test
        @DisplayName("dado orden duplicada → ignora el mensaje sin llamar a Stripe")
        void whenDuplicateOrderId_shouldBeIdempotentAndSkipStripe() throws Exception {
            // arrange
            given(paymentRepository.existsByOrderId(ORDER_ID)).willReturn(true);

            // act
            paymentService.processPayment(buildEvent());

            // assert
            then(stripeGateway).shouldHaveNoInteractions();
            then(paymentRepository).should(never()).save(any());
            then(eventPublisher).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("dado error de Stripe → guarda pago REJECTED y publica PaymentFailedEvent")
        void whenStripeFails_shouldRejectAndPublishFailed() throws Exception {
            // arrange
            given(paymentRepository.existsByOrderId(ORDER_ID)).willReturn(false);

            Payment savedPayment = new Payment();
            savedPayment.setId(UUID.randomUUID());
            savedPayment.setOrderId(ORDER_ID);
            savedPayment.setStatus(PaymentStatus.PENDING);
            given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);

            CardException stripeError = mock(CardException.class);
            given(stripeError.getMessage()).willReturn("Tu tarjeta fue rechazada.");
            given(stripeGateway.charge(any(), anyString(), anyString(), any()))
                    .willThrow(stripeError);

            // act
            paymentService.processPayment(buildEvent());

            // assert
            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            then(paymentRepository).should(times(2)).save(paymentCaptor.capture());

            ArgumentCaptor<PaymentFailedEvent> failedCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
            then(eventPublisher).should().publishPaymentFailed(failedCaptor.capture());
            assertThat(failedCaptor.getValue().getOrderId()).isEqualTo(ORDER_ID);
            assertThat(failedCaptor.getValue().getReason()).contains("rechazada");

            then(eventPublisher).should(never()).publishPaymentCompleted(any());
        }

        @Test
        @DisplayName("dado estado Stripe inesperado (requires_action) → rechaza y publica PaymentFailedEvent")
        void whenStripeStatusIsUnexpected_shouldRejectAndPublishFailed() throws Exception {
            // arrange
            given(paymentRepository.existsByOrderId(ORDER_ID)).willReturn(false);

            Payment savedPayment = new Payment();
            savedPayment.setId(UUID.randomUUID());
            savedPayment.setStatus(PaymentStatus.PENDING);
            given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);

            PaymentIntent mockIntent = mock(PaymentIntent.class);
            given(mockIntent.getStatus()).willReturn("requires_action");
            given(stripeGateway.charge(any(), anyString(), anyString(), any())).willReturn(mockIntent);

            // act
            paymentService.processPayment(buildEvent());

            // assert
            ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
            then(eventPublisher).should().publishPaymentFailed(captor.capture());
            assertThat(captor.getValue().getReason()).contains("requires_action");

            then(eventPublisher).should(never()).publishPaymentCompleted(any());
        }
    }

    @Nested
    @DisplayName("findByOrderId")
    class FindByOrderId {

        @Test
        @DisplayName("dado orderId existente → retorna PaymentResponse")
        void whenExists_shouldReturnResponse() {
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID());
            payment.setOrderId(ORDER_ID);
            payment.setUserId(USER_ID);
            payment.setAmount(AMOUNT);
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setCurrency("MXN");

            given(paymentRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(payment));

            var response = paymentService.findByOrderId(ORDER_ID);

            assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        }

        @Test
        @DisplayName("dado orderId inexistente → lanza PaymentNotFoundException")
        void whenNotExists_shouldThrow() {
            given(paymentRepository.findByOrderId(ORDER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.findByOrderId(ORDER_ID))
                    .isInstanceOf(PaymentNotFoundException.class);
        }
    }
}
