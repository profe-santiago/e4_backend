package com.tickets.payment_service.payment.application;

import com.tickets.payment_service.exception.PaymentNotFoundException;
import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.Payment;
import com.tickets.payment_service.payment.domain.PaymentChargeResult;
import com.tickets.payment_service.payment.domain.PaymentStatus;
import com.tickets.payment_service.payment.domain.UserId;
import com.tickets.payment_service.payment.domain.port.PaymentEventPort;
import com.tickets.payment_service.payment.domain.port.PaymentGateway;
import com.tickets.payment_service.payment.domain.port.PaymentRepository;
import com.tickets.payment_service.shared.annotation.UseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Caso de uso: procesar el reembolso de un pago aprobado.
 *
 * Disparado por RefundInitiatedEvent desde ticket-service.
 * Llama al gateway de Stripe para hacer el refund y publica el resultado.
 *
 * En caso de éxito: payment → REFUNDED, publica RefundCompletedEvent.
 * En caso de fallo: publica RefundFailedEvent (la orden queda en CONFIRMED).
 */
@UseCase
public class RefundPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefundPaymentUseCase.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentEventPort paymentEventPort;

    public RefundPaymentUseCase(PaymentRepository paymentRepository,
                                PaymentGateway paymentGateway,
                                PaymentEventPort paymentEventPort) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.paymentEventPort = paymentEventPort;
    }

    public void execute(UUID orderId, UUID userId) {
        OrderId orderIdVO = OrderId.of(orderId);
        UserId userIdVO = UserId.of(userId);

        Payment payment = paymentRepository.findByOrderId(orderIdVO)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for order: " + orderId));

        // Idempotencia: si ya fue reembolsado, republicar el evento por si no llegó
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            log.warn("[UC] RefundPayment — pago ya REFUNDED, republicando evento: orderId={}", orderId);
            paymentEventPort.publishRefundCompleted(orderIdVO, payment.getUserId());
            return;
        }

        if (payment.getStatus() != PaymentStatus.APPROVED) {
            log.warn("[UC] RefundPayment — pago no está en APPROVED: orderId={}, status={}",
                    orderId, payment.getStatus());
            paymentEventPort.publishRefundFailed(orderIdVO, userIdVO,
                    "El pago no está en estado APPROVED: " + payment.getStatus());
            return;
        }

        log.info("[UC] RefundPayment — iniciando refund en Stripe: orderId={}, transactionId={}",
                orderId, payment.getTransactionId());

        PaymentChargeResult result = paymentGateway.refund(payment.getTransactionId(), orderIdVO);

        if (result.succeeded()) {
            payment.refund();
            paymentRepository.save(payment);
            paymentEventPort.publishRefundCompleted(orderIdVO, payment.getUserId());
            log.info("[UC] RefundPayment — reembolso exitoso: orderId={}", orderId);
        } else {
            paymentEventPort.publishRefundFailed(orderIdVO, payment.getUserId(), result.failureReason());
            log.warn("[UC] RefundPayment — reembolso fallido: orderId={}, reason={}",
                    orderId, result.failureReason());
        }
    }
}
