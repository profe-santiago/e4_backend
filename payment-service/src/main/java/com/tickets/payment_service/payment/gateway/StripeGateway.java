package com.tickets.payment_service.payment.gateway;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto de salida hacia el proveedor de pagos Stripe.
 * Al ser una interfaz, el dominio no depende del SDK concreto — DIP aplicado.
 * Permite mockear en tests sin necesidad de llamadas reales a Stripe.
 */
public interface StripeGateway {

    /**
     * Crea y confirma un PaymentIntent en Stripe.
     *
     * @param amount          monto a cobrar
     * @param currency        moneda ISO 4217 (ej: "MXN")
     * @param paymentMethodId ID del método de pago de Stripe (pm_xxxxx)
     * @param orderId         usado como idempotency key para evitar cargos duplicados
     * @return PaymentIntent confirmado por Stripe
     * @throws StripeException si Stripe rechaza el cobro o hay un error de red
     */
    PaymentIntent charge(BigDecimal amount, String currency, String paymentMethodId, UUID orderId)
            throws StripeException;
}
