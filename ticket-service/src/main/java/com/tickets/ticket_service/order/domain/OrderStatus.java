package com.tickets.ticket_service.order.domain;

import java.util.Set;

/**
 * State Machine del ciclo de vida de una orden.
 * Patrón State embebido en el enum — cada estado define sus transiciones válidas.
 *
 * PENDING   → CONFIRMED (stock reservado + tickets generados)
 * PENDING   → FAILED    (sin stock o pago rechazado)
 * PENDING   → CANCELLED (cancelación manual del usuario)
 * CONFIRMED → REFUNDED
 * FAILED    → (terminal)
 * CANCELLED → (terminal)
 * REFUNDED  → (terminal)
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
            return Set.of(REFUNDED);
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
