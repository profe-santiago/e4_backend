package com.tickets.payment_service.payment.infrastructure.gateway;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.tickets.payment_service.exception.PaymentGatewayException;
import com.tickets.payment_service.payment.domain.CreateIntentResult;
import com.tickets.payment_service.payment.domain.Money;
import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.PaymentChargeResult;
import com.tickets.payment_service.payment.domain.port.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGateway.class);

    private final StripeClient stripeClient;

    StripePaymentGateway(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    @Override
    public CreateIntentResult createIntent(Money amount) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount.toMinorUnits())
                    .setCurrency(amount.currency().toLowerCase(Locale.ROOT))
                    .build();

            PaymentIntent intent = stripeClient.paymentIntents().create(params);
            log.info("[STRIPE] PaymentIntent created: id={}", intent.getId());
            return new CreateIntentResult(intent.getClientSecret(), intent.getId());

        } catch (StripeException e) {
            log.error("[STRIPE] Failed to create PaymentIntent: {}", e.getMessage());
            throw new PaymentGatewayException("No se pudo crear el intento de pago", e);
        }
    }

    @Override
    public PaymentChargeResult charge(Money expectedAmount, String paymentIntentId, OrderId orderId) {
        try {
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("verify-order-" + orderId.value())
                    .build();

            PaymentIntent intent = stripeClient.paymentIntents().retrieve(paymentIntentId, options);
            log.info("[STRIPE] PaymentIntent retrieved: id={}, status={}", intent.getId(), intent.getStatus());

            if (!"succeeded".equals(intent.getStatus())) {
                log.warn("[STRIPE] PaymentIntent not succeeded: id={}, status={}", intent.getId(), intent.getStatus());
                return PaymentChargeResult.failure("Estado de pago inesperado: " + intent.getStatus());
            }

            // Validar que el monto cobrado coincida con el de la orden (anti-manipulación)
            long expectedMinorUnits = expectedAmount.toMinorUnits();
            if (!intent.getAmount().equals(expectedMinorUnits)) {
                log.error("[STRIPE] Amount mismatch for orderId={}: expected={}, got={}",
                        orderId.value(), expectedMinorUnits, intent.getAmount());
                return PaymentChargeResult.failure(
                        "El monto cobrado no coincide con el total de la orden");
            }

            return PaymentChargeResult.success(intent.getId());

        } catch (StripeException e) {
            log.error("[STRIPE] Verification failed for orderId={}: {}", orderId.value(), e.getMessage());
            return PaymentChargeResult.failure(e.getMessage());
        }
    }

    @Override
    public PaymentChargeResult refund(String transactionId, OrderId orderId) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(transactionId)
                    .build();

            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("refund-order-" + orderId.value())
                    .build();

            Refund refund = stripeClient.refunds().create(params, options);
            log.info("[STRIPE] Refund created: id={}, status={}", refund.getId(), refund.getStatus());

            if ("succeeded".equals(refund.getStatus()) || "pending".equals(refund.getStatus())) {
                return PaymentChargeResult.success(refund.getId());
            }

            log.warn("[STRIPE] Unexpected refund status for orderId={}: {}", orderId.value(), refund.getStatus());
            return PaymentChargeResult.failure("Unexpected refund status: " + refund.getStatus());

        } catch (StripeException e) {
            log.error("[STRIPE] Refund failed for orderId={}: {}", orderId.value(), e.getMessage());
            return PaymentChargeResult.failure(e.getMessage());
        }
    }
}
