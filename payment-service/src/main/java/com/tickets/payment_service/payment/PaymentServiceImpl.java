package com.tickets.payment_service.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.tickets.payment_service.exception.PaymentNotFoundException;
import com.tickets.payment_service.messaging.event.OrderConfirmedEvent;
import com.tickets.payment_service.messaging.event.PaymentCompletedEvent;
import com.tickets.payment_service.messaging.event.PaymentFailedEvent;
import com.tickets.payment_service.messaging.publisher.PaymentEventPublisher;
import com.tickets.payment_service.payment.dto.PaymentResponse;
import com.tickets.payment_service.payment.gateway.StripeGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final StripeGateway stripeGateway;
    private final PaymentEventPublisher eventPublisher;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                               StripeGateway stripeGateway,
                               PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.stripeGateway     = stripeGateway;
        this.eventPublisher    = eventPublisher;
    }

    @Override
    @Transactional
    public void processPayment(OrderConfirmedEvent event) {
        // Idempotencia: si ya procesamos este orderId, ignoramos el mensaje duplicado
        if (paymentRepository.existsByOrderId(event.getOrderId())) {
            log.warn("[PAYMENT] Orden ya procesada, ignorando mensaje duplicado → orderId={}", event.getOrderId());
            return;
        }

        Payment payment = buildPendingPayment(event);
        paymentRepository.save(payment);

        try {
            PaymentIntent intent = stripeGateway.charge(
                    event.getTotalAmount(),
                    payment.getCurrency(),
                    event.getPaymentMethodId(),
                    event.getOrderId()
            );

            if ("succeeded".equals(intent.getStatus())) {
                approvePayment(payment, intent);
                eventPublisher.publishPaymentCompleted(new PaymentCompletedEvent(
                        event.getOrderId(), event.getUserId(), payment.getId(), intent.getId()));
                log.info("[PAYMENT] Pago aprobado → orderId={}, intentId={}", event.getOrderId(), intent.getId());
            } else {
                // Estado no terminal pero no exitoso (ej: requires_action sin soporte 3DS)
                rejectPayment(payment, "Estado Stripe inesperado: " + intent.getStatus());
                eventPublisher.publishPaymentFailed(new PaymentFailedEvent(
                        event.getOrderId(), event.getUserId(),
                        "Estado Stripe inesperado: " + intent.getStatus()));
            }

        } catch (StripeException e) {
            log.error("[PAYMENT] Error Stripe → orderId={}, mensaje={}", event.getOrderId(), e.getMessage());
            rejectPayment(payment, e.getMessage());
            eventPublisher.publishPaymentFailed(new PaymentFailedEvent(
                    event.getOrderId(), event.getUserId(), e.getMessage()));
        }
    }

    @Override
    public PaymentResponse findByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(PaymentMapper::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException("orderId=" + orderId));
    }

    @Override
    public PaymentResponse findById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .map(PaymentMapper::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException("id=" + paymentId));
    }

    // ── helpers privados ──────────────────────────────────────────────────────

    private Payment buildPendingPayment(OrderConfirmedEvent event) {
        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setUserId(event.getUserId());
        payment.setAmount(event.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    private void approvePayment(Payment payment, PaymentIntent intent) {
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setTransactionId(intent.getId());
        payment.setPaymentMethod(resolvePaymentMethodType(intent.getPaymentMethodTypes()));
        paymentRepository.save(payment);
    }

    private void rejectPayment(Payment payment, String reason) {
        log.warn("[PAYMENT] Pago rechazado → paymentId={}, reason={}", payment.getId(), reason);
        payment.setStatus(PaymentStatus.REJECTED);
        paymentRepository.save(payment);
    }

    private String resolvePaymentMethodType(java.util.List<String> types) {
        if (types == null || types.isEmpty()) return "UNKNOWN";
        return switch (types.get(0)) {
            case "card"         -> "CREDIT_CARD";
            case "bank_transfer"-> "TRANSFER";
            default             -> types.get(0).toUpperCase();
        };
    }
}
