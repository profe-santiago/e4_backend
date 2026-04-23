package com.tickets.payment_service.payment.infrastructure.gateway;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.tickets.payment_service.payment.domain.Money;
import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.PaymentChargeResult;
import com.tickets.payment_service.payment.domain.port.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Adaptador de salida que implementa PaymentGateway usando el SDK de Stripe.
 *
 * Usa StripeClient (inyectado como bean) en lugar de la API estática legacy.
 * Cualquier excepción de Stripe queda encapsulada en PaymentChargeResult.failure()
 * — el dominio nunca ve StripeException.
 *
 * Para reemplazar Stripe por otro proveedor basta con crear otra implementación
 * de PaymentGateway y cambiar el bean en la configuración.
 */
@Component
class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGateway.class);

    private final StripeClient stripeClient;

    StripePaymentGateway(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    @Override
    public PaymentChargeResult charge(Money amount, String paymentMethodId, OrderId orderId) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount.toMinorUnits())
                    .setCurrency(amount.currency().toLowerCase(Locale.ROOT))
                    .setPaymentMethod(paymentMethodId)
                    .setConfirm(true)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(
                                            PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .build();

            // La idempotency key garantiza que reintentos del mismo pedido no cobren dos veces
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("pay-order-" + orderId.value())
                    .build();

            PaymentIntent intent = stripeClient.paymentIntents().create(params, options);
            log.info("[STRIPE] PaymentIntent created: id={}, status={}", intent.getId(), intent.getStatus());

            if ("succeeded".equals(intent.getStatus())) {
                return PaymentChargeResult.success(intent.getId());
            }

            log.warn("[STRIPE] Unexpected status for orderId={}: {}", orderId.value(), intent.getStatus());
            return PaymentChargeResult.failure("Unexpected payment status: " + intent.getStatus());

        } catch (StripeException e) {
            log.error("[STRIPE] Charge failed for orderId={}: {}", orderId.value(), e.getMessage());
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
