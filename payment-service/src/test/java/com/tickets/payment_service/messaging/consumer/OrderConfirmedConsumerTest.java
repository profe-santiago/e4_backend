package com.tickets.payment_service.messaging.consumer;

import com.tickets.payment_service.messaging.event.OrderConfirmedEvent;
import com.tickets.payment_service.payment.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderConfirmedConsumer")
class OrderConfirmedConsumerTest {

    @Mock  private PaymentService paymentService;
    @InjectMocks private OrderConfirmedConsumer consumer;

    @Test
    @DisplayName("al recibir OrderConfirmedEvent → delega al PaymentService")
    void handle_shouldDelegateToPaymentService() {
        OrderConfirmedEvent event = new OrderConfirmedEvent(
                UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("250.00"), "pm_test_visa", List.of());

        consumer.handle(event);

        then(paymentService).should().processPayment(event);
    }
}
