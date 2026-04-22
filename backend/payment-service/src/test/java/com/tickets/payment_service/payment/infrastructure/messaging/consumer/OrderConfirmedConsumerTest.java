package com.tickets.payment_service.payment.infrastructure.messaging.consumer;

import com.tickets.payment_service.payment.application.ProcessPaymentUseCase;
import com.tickets.payment_service.payment.application.dto.ProcessPaymentCommand;
import com.tickets.payment_service.payment.infrastructure.messaging.event.OrderConfirmedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderConfirmedConsumer")
class OrderConfirmedConsumerTest {

    @Mock  private ProcessPaymentUseCase processPaymentUseCase;
    @InjectMocks private OrderConfirmedConsumer consumer;

    @Test
    @DisplayName("al recibir OrderConfirmedEvent → construye ProcessPaymentCommand y delega al use case")
    void handle_shouldBuildCommandAndDelegateToUseCase() {
        UUID orderId = UUID.randomUUID();
        UUID userId  = UUID.randomUUID();

        OrderConfirmedEvent event = new OrderConfirmedEvent(
                orderId, userId, new BigDecimal("250.00"), "pm_test_visa", List.of());

        consumer.handle(event);

        ArgumentCaptor<ProcessPaymentCommand> captor = ArgumentCaptor.forClass(ProcessPaymentCommand.class);
        then(processPaymentUseCase).should().execute(captor.capture());

        ProcessPaymentCommand command = captor.getValue();
        assertThat(command.orderId()).isEqualTo(orderId);
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.amount()).isEqualByComparingTo("250.00");
        assertThat(command.currency()).isEqualTo("MXN");
        assertThat(command.paymentMethodId()).isEqualTo("pm_test_visa");
    }
}
