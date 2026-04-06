package com.tickets.payment_service.payment.gateway;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Adaptador concreto que encapsula el SDK de Stripe.
 * El resto del dominio solo conoce la interfaz StripeGateway.
 */
@Component
public class StripeGatewayImpl implements StripeGateway {

    private static final Logger log = LoggerFactory.getLogger(StripeGatewayImpl.class);

    @Override
    public PaymentIntent charge(BigDecimal amount, String currency, String paymentMethodId, UUID orderId)
            throws StripeException {

        // Stripe opera en centavos (o la unidad menor de la moneda)
        long amountInMinorUnits = amount
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInMinorUnits)
                .setCurrency(currency.toLowerCase())
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)
                // Evita 3DS redirects — útil en flujos server-to-server
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(
                                        PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build())
                .build();

        // La idempotency key previene cargos duplicados si el mensaje se reprocesa
        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey("pay-order-" + orderId)
                .build();

        log.info("[STRIPE] Creando PaymentIntent → orderId={}, amount={} {}", orderId, amount, currency);
        PaymentIntent intent = PaymentIntent.create(params, requestOptions);
        log.info("[STRIPE] PaymentIntent creado → id={}, status={}", intent.getId(), intent.getStatus());

        return intent;
    }
}
