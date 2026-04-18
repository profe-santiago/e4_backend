package com.tickets.payment_service.payment.domain.port;

import com.tickets.payment_service.payment.domain.Money;
import com.tickets.payment_service.payment.domain.OrderId;
import com.tickets.payment_service.payment.domain.PaymentChargeResult;

/**
 * Puerto secundario (salida) hacia el proveedor de pagos.
 *
 * Aplica DIP: el dominio depende de esta abstracción, no de Stripe.
 * La implementación concreta (StripePaymentGateway) vive en infraestructura
 * y puede reemplazarse por cualquier otro proveedor sin tocar el dominio.
 */
public interface PaymentGateway {

    /**
     * Intenta cobrar el monto al método de pago dado.
     *
     * @param amount          monto y moneda a cobrar
     * @param paymentMethodId identificador del método de pago del proveedor (e.g. pm_xxxxx de Stripe)
     * @param orderId         identificador de la orden; se usa como clave de idempotencia
     * @return resultado del cobro — nunca lanza excepción; los errores quedan en {@code failureReason}
     */
    PaymentChargeResult charge(Money amount, String paymentMethodId, OrderId orderId);

    /**
     * Reembolsa un pago previamente aprobado.
     *
     * @param transactionId ID del PaymentIntent de Stripe a reembolsar
     * @param orderId       identificador de la orden; se usa como clave de idempotencia del refund
     * @return resultado del reembolso — nunca lanza excepción; los errores quedan en {@code failureReason}
     */
    PaymentChargeResult refund(String transactionId, OrderId orderId);
}
