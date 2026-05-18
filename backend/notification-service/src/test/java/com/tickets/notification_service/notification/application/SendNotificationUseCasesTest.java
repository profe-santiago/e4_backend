package com.tickets.notification_service.notification.application;

import com.tickets.notification_service.notification.application.dto.SendOrderCancelledCommand;
import com.tickets.notification_service.notification.application.dto.SendOrderConfirmedCommand;
import com.tickets.notification_service.notification.application.dto.SendPaymentCompletedCommand;
import com.tickets.notification_service.notification.application.dto.SendRefundCompletedCommand;
import com.tickets.notification_service.notification.application.dto.SendRefundFailedCommand;
import com.tickets.notification_service.notification.domain.Notification;
import com.tickets.notification_service.notification.domain.NotificationStatus;
import com.tickets.notification_service.notification.domain.NotificationType;
import com.tickets.notification_service.notification.domain.UserInfo;
import com.tickets.notification_service.notification.domain.port.NotificationEmailPort;
import com.tickets.notification_service.notification.domain.port.NotificationRepository;
import com.tickets.notification_service.notification.domain.port.UserGateway;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("Send Notification UseCases")
class SendNotificationUseCasesTest {

    @Mock private NotificationRepository repository;
    @Mock private UserGateway userGateway;
    @Mock private NotificationEmailPort emailPort;

    private UUID orderId;
    private UUID userId;
    private UserInfo knownUser;

    @BeforeEach
    void setUp() {
        orderId   = UUID.randomUUID();
        userId    = UUID.randomUUID();
        knownUser = new UserInfo("Juan", "juan@test.com");
        given(repository.save(any(Notification.class))).willAnswer(inv -> inv.getArgument(0));
    }

    // ── SendPaymentCompletedNotificationUseCase ──────────────────────────────

    @Nested
    @DisplayName("SendPaymentCompletedNotificationUseCase")
    class PaymentCompleted {

        private SendPaymentCompletedNotificationUseCase useCase;
        private UUID paymentId;
        private String stripeId;

        @BeforeEach
        void init() {
            useCase   = new SendPaymentCompletedNotificationUseCase(repository, userGateway, emailPort);
            paymentId = UUID.randomUUID();
            stripeId  = "pi_test_stripe123";
        }

        @Test
        @DisplayName("usuario encontrado → envía email y marca notificación SENT")
        void shouldSendEmail_andMarkSent_whenUserFound() {
            given(userGateway.findById(any())).willReturn(Optional.of(knownUser));

            useCase.execute(new SendPaymentCompletedCommand(orderId, userId, paymentId, stripeId));

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(captor.getValue().getType()).isEqualTo(NotificationType.PAYMENT_CONFIRMED);
            then(emailPort).should().sendPaymentCompleted(
                    knownUser.email(), knownUser.firstName(), orderId, paymentId, stripeId);
        }

        @Test
        @DisplayName("usuario no encontrado → marca notificación FAILED sin enviar email")
        void shouldMarkFailed_whenUserNotFound() {
            given(userGateway.findById(any())).willReturn(Optional.empty());

            useCase.execute(new SendPaymentCompletedCommand(orderId, userId, paymentId, stripeId));

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
            then(emailPort).should(never()).sendPaymentCompleted(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("fallo al enviar email → marca notificación FAILED y guarda")
        void shouldMarkFailed_whenEmailThrows() {
            given(userGateway.findById(any())).willReturn(Optional.of(knownUser));
            willThrow(new RuntimeException("SMTP error"))
                    .given(emailPort).sendPaymentCompleted(any(), any(), any(), any(), any());

            useCase.execute(new SendPaymentCompletedCommand(orderId, userId, paymentId, stripeId));

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
        }
    }

    // ── SendOrderConfirmedNotificationUseCase ────────────────────────────────

    @Nested
    @DisplayName("SendOrderConfirmedNotificationUseCase")
    class OrderConfirmed {

        private SendOrderConfirmedNotificationUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new SendOrderConfirmedNotificationUseCase(repository, userGateway, emailPort);
        }

        @Test
        @DisplayName("usuario encontrado → envía email con tickets y marca SENT")
        void shouldSendEmail_andMarkSent_whenUserFound() {
            given(userGateway.findById(any())).willReturn(Optional.of(knownUser));
            List<SendOrderConfirmedCommand.ConfirmedTicket> tickets = List.of(
                    new SendOrderConfirmedCommand.ConfirmedTicket(
                            UUID.randomUUID(), UUID.randomUUID(), 1L, "QR123"));

            useCase.execute(new SendOrderConfirmedCommand(orderId, userId,
                    new BigDecimal("500.00"), tickets));

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(captor.getValue().getType()).isEqualTo(NotificationType.PURCHASE_CONFIRMATION);
            then(emailPort).should().sendOrderConfirmed(
                    knownUser.email(), knownUser.firstName(), orderId, new BigDecimal("500.00"), 1);
        }

        @Test
        @DisplayName("usuario no encontrado → marca FAILED sin enviar email")
        void shouldMarkFailed_whenUserNotFound() {
            given(userGateway.findById(any())).willReturn(Optional.empty());

            useCase.execute(new SendOrderConfirmedCommand(orderId, userId,
                    new BigDecimal("500.00"), List.of()));

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
            then(emailPort).should(never()).sendOrderConfirmed(any(), any(), any(), any(), any(Integer.class));
        }

        @Test
        @DisplayName("fallo al enviar email → marca FAILED y guarda")
        void shouldMarkFailed_whenEmailThrows() {
            given(userGateway.findById(any())).willReturn(Optional.of(knownUser));
            willThrow(new RuntimeException("SMTP error"))
                    .given(emailPort).sendOrderConfirmed(any(), any(), any(), any(), any(Integer.class));

            useCase.execute(new SendOrderConfirmedCommand(orderId, userId,
                    new BigDecimal("500.00"), List.of()));

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
        }
    }

    // ── SendOrderCancelledNotificationUseCase ────────────────────────────────

    @Nested
    @DisplayName("SendOrderCancelledNotificationUseCase")
    class OrderCancelled {

        private SendOrderCancelledNotificationUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new SendOrderCancelledNotificationUseCase(repository, userGateway, emailPort);
        }

        @Test
        @DisplayName("usuario encontrado → envía email de cancelación y marca SENT")
        void shouldSendEmail_andMarkSent_whenUserFound() {
            given(userGateway.findById(any())).willReturn(Optional.of(knownUser));

            useCase.execute(new SendOrderCancelledCommand(orderId, userId, "Sin stock disponible"));

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(captor.getValue().getType()).isEqualTo(NotificationType.EVENT_CANCELLED);
            then(emailPort).should().sendOrderCancelled(
                    knownUser.email(), knownUser.firstName(), orderId, "Sin stock disponible");
        }

        @Test
        @DisplayName("reason nulo → usa mensaje por defecto")
        void shouldUseDefaultReason_whenReasonIsNull() {
            given(userGateway.findById(any())).willReturn(Optional.of(knownUser));

            useCase.execute(new SendOrderCancelledCommand(orderId, userId, null));

            then(emailPort).should()
                    .sendOrderCancelled(any(), any(), any(), org.mockito.ArgumentMatchers.contains("Error"));
        }

        @Test
        @DisplayName("usuario no encontrado → marca FAILED sin enviar email")
        void shouldMarkFailed_whenUserNotFound() {
            given(userGateway.findById(any())).willReturn(Optional.empty());

            useCase.execute(new SendOrderCancelledCommand(orderId, userId, "Sin stock"));

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
            then(emailPort).should(never()).sendOrderCancelled(any(), any(), any(), any());
        }
    }

    // ── SendRefundCompletedNotificationUseCase ───────────────────────────────

    @Nested
    @DisplayName("SendRefundCompletedNotificationUseCase")
    class RefundCompleted {

        private SendRefundCompletedNotificationUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new SendRefundCompletedNotificationUseCase(repository, userGateway, emailPort);
        }

        @Test
        @DisplayName("usuario encontrado → envía email de reembolso y marca SENT")
        void shouldSendEmail_andMarkSent_whenUserFound() {
            given(userGateway.findById(any())).willReturn(Optional.of(knownUser));

            useCase.execute(new SendRefundCompletedCommand(orderId, userId));

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(captor.getValue().getType()).isEqualTo(NotificationType.REFUND_COMPLETED);
            then(emailPort).should().sendRefundCompleted(knownUser.email(), knownUser.firstName(), orderId);
        }

        @Test
        @DisplayName("usuario no encontrado → marca FAILED sin enviar email")
        void shouldMarkFailed_whenUserNotFound() {
            given(userGateway.findById(any())).willReturn(Optional.empty());

            useCase.execute(new SendRefundCompletedCommand(orderId, userId));

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
            then(emailPort).should(never()).sendRefundCompleted(any(), any(), any());
        }
    }

    // ── SendRefundFailedNotificationUseCase ──────────────────────────────────

    @Nested
    @DisplayName("SendRefundFailedNotificationUseCase")
    class RefundFailed {

        private SendRefundFailedNotificationUseCase useCase;

        @BeforeEach
        void init() {
            useCase = new SendRefundFailedNotificationUseCase(repository, userGateway, emailPort);
        }

        @Test
        @DisplayName("usuario encontrado → envía email de fallo de reembolso y marca SENT")
        void shouldSendEmail_andMarkSent_whenUserFound() {
            given(userGateway.findById(any())).willReturn(Optional.of(knownUser));

            useCase.execute(new SendRefundFailedCommand(orderId, userId, "Tarjeta expirada"));

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(captor.getValue().getType()).isEqualTo(NotificationType.REFUND_FAILED);
            then(emailPort).should().sendRefundFailed(
                    knownUser.email(), knownUser.firstName(), orderId, "Tarjeta expirada");
        }

        @Test
        @DisplayName("reason nulo → usa mensaje por defecto")
        void shouldUseDefaultReason_whenReasonIsNull() {
            given(userGateway.findById(any())).willReturn(Optional.of(knownUser));

            useCase.execute(new SendRefundFailedCommand(orderId, userId, null));

            then(emailPort).should()
                    .sendRefundFailed(any(), any(), any(), org.mockito.ArgumentMatchers.contains("Error"));
        }

        @Test
        @DisplayName("usuario no encontrado → marca FAILED sin enviar email")
        void shouldMarkFailed_whenUserNotFound() {
            given(userGateway.findById(any())).willReturn(Optional.empty());

            useCase.execute(new SendRefundFailedCommand(orderId, userId, "Tarjeta expirada"));

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            then(repository).should().save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
            then(emailPort).should(never()).sendRefundFailed(any(), any(), any(), any());
        }
    }
}
