package com.tickets.ticket_service.order;

import java.util.Set;

/**
 * State Machine del ciclo de vida de una orden.
 *
 * PENDING   → CONFIRMED (stock reservado + tickets generados)
 * PENDING   → FAILED    (sin stock)
 * PENDING   → CANCELLED (cancelación manual del usuario)
 * CONFIRMED → REFUNDED  (solicitud de reembolso)
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
