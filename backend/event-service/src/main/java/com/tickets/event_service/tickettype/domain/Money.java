package com.tickets.event_service.tickettype.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object que representa un monto monetario.
 * Inmutable — garantiza invariantes en el constructor.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount, "amount no puede ser nulo");
        Objects.requireNonNull(currency, "currency no puede ser nulo");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money no puede ser negativo");
        }
        if (currency.isBlank()) {
            throw new IllegalArgumentException("Currency es obligatorio");
        }
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money ofUSD(BigDecimal amount) {
        return new Money(amount, "USD");
    }

    public Money multiply(int quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("No se pueden sumar monedas distintas: "
                    + this.currency + " y " + other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
