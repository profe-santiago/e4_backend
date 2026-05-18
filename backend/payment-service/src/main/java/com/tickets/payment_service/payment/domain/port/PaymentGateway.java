package com.tickets.payment_service.payment.domain.port;

import com.tickets.payment_service.payment.domain.CreateIntentResult;
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
     * Crea un PaymentIntent sin confirmarlo — el frontend lo confirma con 3DS si es necesario.
     *
     * @param amount monto y moneda del intento
     * @return clientSecret (para confirmar en el browser) + paymentIntentId
     */
    CreateIntentResult createIntent(Money amount);

    /**
     * Verifica que un PaymentIntent ya confirmado por el frontend haya tenido éxito.
     * Valida status y que el monto coincida con el esperado.
     *
     * @param paymentIntentId ID del PaymentIntent confirmado por el frontend (pi_xxxxx)
     * @param expectedAmount  monto esperado según la orden (protección contra manipulación)
     * @param orderId         identificador de la orden; se usa como clave de idempotencia
     * @return resultado de la verificación — nunca lanza excepción
     */
    PaymentChargeResult charge(Money expectedAmount, String paymentIntentId, OrderId orderId);

    /**
     * Reembolsa un pago previamente aprobado.
     *
     * @param transactionId ID del PaymentIntent de Stripe a reembolsar
     * @param orderId       identificador de la orden; se usa como clave de idempotencia del refund
     * @return resultado del reembolso — nunca lanza excepción; los errores quedan en {@code failureReason}
     */
    PaymentChargeResult refund(String transactionId, OrderId orderId);
}
