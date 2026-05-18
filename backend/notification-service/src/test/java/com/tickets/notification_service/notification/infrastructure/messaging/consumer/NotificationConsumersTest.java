package com.tickets.notification_service.notification.infrastructure.messaging.consumer;

import com.tickets.notification_service.notification.application.SendOrderCancelledNotificationUseCase;
import com.tickets.notification_service.notification.application.SendOrderConfirmedNotificationUseCase;
import com.tickets.notification_service.notification.application.SendPaymentCompletedNotificationUseCase;
import com.tickets.notification_service.notification.application.SendRefundCompletedNotificationUseCase;
import com.tickets.notification_service.notification.application.SendRefundFailedNotificationUseCase;
import com.tickets.notification_service.notification.application.dto.SendOrderCancelledCommand;
import com.tickets.notification_service.notification.application.dto.SendOrderConfirmedCommand;
import com.tickets.notification_service.notification.application.dto.SendPaymentCompletedCommand;
import com.tickets.notification_service.notification.application.dto.SendRefundCompletedCommand;
import com.tickets.notification_service.notification.application.dto.SendRefundFailedCommand;
import com.tickets.notification_service.notification.infrastructure.messaging.event.OrderCancelledEvent;
import com.tickets.notification_service.notification.infrastructure.messaging.event.OrderConfirmedEvent;
import com.tickets.notification_service.notification.infrastructure.messaging.event.PaymentCompletedEvent;
import com.tickets.notification_service.notification.infrastructure.messaging.event.RefundCompletedEvent;
import com.tickets.notification_service.notification.infrastructure.messaging.event.RefundFailedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Consumers")
class NotificationConsumersTest {

    // ── OrderConfirmedConsumer ───────────────────────────────────────────────

    @Nested
    @DisplayName("OrderConfirmedConsumer")
    class OrderConfirmedConsumerTests {

        @Mock private SendOrderConfirmedNotificationUseCase useCase;
        private OrderConfirmedConsumer consumer;

        @BeforeEach
        void init() {
            consumer = new OrderConfirmedConsumer(useCase);
        }

        @Test
        @DisplayName("debe mapear tickets y delegar el command correcto al use case")
        void shouldMapTickets_andDelegate() {
            UUID orderId  = UUID.randomUUID();
            UUID userId   = UUID.randomUUID();
            UUID ticketId = UUID.randomUUID();
            UUID eventId  = UUID.randomUUID();

            OrderConfirmedEvent.ConfirmedTicket ticket =
                    new OrderConfirmedEvent.ConfirmedTicket(ticketId, eventId, 1L, "QR_CODE");
            OrderConfirmedEvent event = new OrderConfirmedEvent(
                    orderId, userId, new BigDecimal("350.00"), "pm_card", List.of(ticket));

            consumer.handle(event);

            ArgumentCaptor<SendOrderConfirmedCommand> captor =
                    ArgumentCaptor.forClass(SendOrderConfirmedCommand.class);
            then(useCase).should().execute(captor.capture());

            SendOrderConfirmedCommand cmd = captor.getValue();
            assertThat(cmd.orderId()).isEqualTo(orderId);
            assertThat(cmd.userId()).isEqualTo(userId);
            assertThat(cmd.totalAmount()).isEqualByComparingTo("350.00");
            assertThat(cmd.tickets()).hasSize(1);
            assertThat(cmd.tickets().get(0).qrCode()).isEqualTo("QR_CODE");
        }

        @Test
        @DisplayName("tickets null → pasa lista vacía al use case")
        void shouldPassEmptyList_whenTicketsNull() {
            OrderConfirmedEvent event = new OrderConfirmedEvent(
                    UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, "pm_x", null);

            consumer.handle(event);

            ArgumentCaptor<SendOrderConfirmedCommand> captor =
                    ArgumentCaptor.forClass(SendOrderConfirmedCommand.class);
            then(useCase).should().execute(captor.capture());
            assertThat(captor.getValue().tickets()).isEmpty();
        }
    }

    // ── PaymentCompletedConsumer ─────────────────────────────────────────────

    @Nested
    @DisplayName("PaymentCompletedConsumer")
    class PaymentCompletedConsumerTests {

        @Mock private SendPaymentCompletedNotificationUseCase useCase;
        private PaymentCompletedConsumer consumer;

        @BeforeEach
        void init() {
            consumer = new PaymentCompletedConsumer(useCase);
        }

        @Test
        @DisplayName("debe mapear todos los campos y delegar al use case")
        void shouldMapAllFields_andDelegate() {
            UUID orderId    = UUID.randomUUID();
            UUID userId     = UUID.randomUUID();
            UUID paymentId  = UUID.randomUUID();
            String stripeId = "pi_test_abc";

            consumer.handle(new PaymentCompletedEvent(orderId, userId, paymentId, stripeId));

            ArgumentCaptor<SendPaymentCompletedCommand> captor =
                    ArgumentCaptor.forClass(SendPaymentCompletedCommand.class);
            then(useCase).should().execute(captor.capture());

            SendPaymentCompletedCommand cmd = captor.getValue();
            assertThat(cmd.orderId()).isEqualTo(orderId);
            assertThat(cmd.userId()).isEqualTo(userId);
            assertThat(cmd.paymentId()).isEqualTo(paymentId);
            assertThat(cmd.stripePaymentIntentId()).isEqualTo(stripeId);
        }
    }

    // ── OrderCancelledConsumer ───────────────────────────────────────────────

    @Nested
    @DisplayName("OrderCancelledConsumer")
    class OrderCancelledConsumerTests {

        @Mock private SendOrderCancelledNotificationUseCase useCase;
        private OrderCancelledConsumer consumer;

        @BeforeEach
        void init() {
            consumer = new OrderCancelledConsumer(useCase);
        }

        @Test
        @DisplayName("debe mapear orderId, userId y reason al command")
        void shouldMapAllFields_andDelegate() {
            UUID orderId = UUID.randomUUID();
            UUID userId  = UUID.randomUUID();

            consumer.handle(new OrderCancelledEvent(orderId, userId, "Sin stock"));

            ArgumentCaptor<SendOrderCancelledCommand> captor =
                    ArgumentCaptor.forClass(SendOrderCancelledCommand.class);
            then(useCase).should().execute(captor.capture());

            SendOrderCancelledCommand cmd = captor.getValue();
            assertThat(cmd.orderId()).isEqualTo(orderId);
            assertThat(cmd.userId()).isEqualTo(userId);
            assertThat(cmd.reason()).isEqualTo("Sin stock");
        }
    }

    // ── RefundCompletedConsumer ──────────────────────────────────────────────

    @Nested
    @DisplayName("RefundCompletedConsumer")
    class RefundCompletedConsumerTests {

        @Mock private SendRefundCompletedNotificationUseCase useCase;
        private RefundCompletedConsumer consumer;

        @BeforeEach
        void init() {
            consumer = new RefundCompletedConsumer(useCase);
        }

        @Test
        @DisplayName("debe mapear orderId y userId al command")
        void shouldMapAllFields_andDelegate() {
            UUID orderId = UUID.randomUUID();
            UUID userId  = UUID.randomUUID();

            consumer.handle(new RefundCompletedEvent(orderId, userId));

            ArgumentCaptor<SendRefundCompletedCommand> captor =
                    ArgumentCaptor.forClass(SendRefundCompletedCommand.class);
            then(useCase).should().execute(captor.capture());

            assertThat(captor.getValue().orderId()).isEqualTo(orderId);
            assertThat(captor.getValue().userId()).isEqualTo(userId);
        }
    }

    // ── RefundFailedConsumer ─────────────────────────────────────────────────

    @Nested
    @DisplayName("RefundFailedConsumer")
    class RefundFailedConsumerTests {

        @Mock private SendRefundFailedNotificationUseCase useCase;
        private RefundFailedConsumer consumer;

        @BeforeEach
        void init() {
            consumer = new RefundFailedConsumer(useCase);
        }

        @Test
        @DisplayName("debe mapear orderId, userId y reason al command")
        void shouldMapAllFields_andDelegate() {
            UUID orderId = UUID.randomUUID();
            UUID userId  = UUID.randomUUID();

            consumer.handle(new RefundFailedEvent(orderId, userId, "Cuenta cerrada"));

            ArgumentCaptor<SendRefundFailedCommand> captor =
                    ArgumentCaptor.forClass(SendRefundFailedCommand.class);
            then(useCase).should().execute(captor.capture());

            SendRefundFailedCommand cmd = captor.getValue();
            assertThat(cmd.orderId()).isEqualTo(orderId);
            assertThat(cmd.userId()).isEqualTo(userId);
            assertThat(cmd.reason()).isEqualTo("Cuenta cerrada");
        }
    }
}
