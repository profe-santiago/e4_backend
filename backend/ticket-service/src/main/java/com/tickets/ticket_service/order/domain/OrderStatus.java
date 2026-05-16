package com.tickets.ticket_service.order.domain;

import java.util.Set;

/**
 * State Machine del ciclo de vida de una orden.
 * Patrón State embebido en el enum — cada estado define sus transiciones válidas.
 *
 * PENDING        → CONFIRMED, FAILED, CANCELLED
 * CONFIRMED      → REFUND_PENDING
 * REFUND_PENDING → REFUNDED  (reembolso aprobado en Stripe)
 * REFUND_PENDING → CONFIRMED (reembolso fallido, permite reintentar)
 * FAILED         → (terminal)
 * CANCELLED      → (terminal)
 * REFUNDED       → (terminal)
 */
public enum OrderStatus {

    PENDING {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return Set.of(CONFIRMED, FAILED, CANCELLED);
        }
    },
    CONFIRMED {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return Set.of(REFUND_PENDING);
        }
    },
    REFUND_PENDING {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return Set.of(REFUNDED, CONFIRMED);
        }
    },
    FAILED {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return Set.of();
        }
    },
    CANCELLED {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return Set.of();
        }
    },
    REFUNDED {
        @Override
        public Set<OrderStatus> allowedTransitions() {
            return Set.of();
        }
    };

    public abstract Set<OrderStatus> allowedTransitions();

    public boolean canTransitionTo(OrderStatus target) {
        return allowedTransitions().contains(target);
    }
}
