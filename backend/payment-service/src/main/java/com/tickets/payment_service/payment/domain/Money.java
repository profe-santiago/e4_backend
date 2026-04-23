package com.tickets.payment_service.payment.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object que representa un monto con su moneda.
 * Encapsula la lógica de conversión a unidades mínimas (centavos)
 * sin acoplar el dominio a ninguna librería de pago.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money amount cannot be negative");
        }
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    /**
     * Convierte el monto a unidades mínimas (e.g. pesos → centavos).
     * Stripe y la mayoría de gateways trabajan con long en unidades mínimas.
     */
    public long toMinorUnits() {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot add Money with different currencies: %s vs %s".formatted(this.currency, other.currency));
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }

}
