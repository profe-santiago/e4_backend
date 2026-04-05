package com.tickets.event_service.event;

import java.util.Set;

/**
 * State Machine del ciclo de vida de un evento.
 * Cada estado define sus transiciones válidas — patrón State embebido en el enum.
 *
 * Grafo de transiciones:
 *   DRAFT → PUBLISHED, CANCELLED
 *   PUBLISHED → SOLD_OUT, CANCELLED, FINISHED
 *   SOLD_OUT → PUBLISHED, CANCELLED, FINISHED
 *   CANCELLED → (terminal)
 *   FINISHED  → (terminal)
 */
public enum EventStatus {

    DRAFT {
        @Override
        public Set<EventStatus> allowedTransitions() {
            return Set.of(PUBLISHED, CANCELLED);
        }
    },
    PUBLISHED {
        @Override
        public Set<EventStatus> allowedTransitions() {
            return Set.of(SOLD_OUT, CANCELLED, FINISHED);
        }
    },
    SOLD_OUT {
        @Override
        public Set<EventStatus> allowedTransitions() {
            return Set.of(PUBLISHED, CANCELLED, FINISHED);
        }
    },
    CANCELLED {
        @Override
        public Set<EventStatus> allowedTransitions() {
            return Set.of();
        }
    },
    FINISHED {
        @Override
        public Set<EventStatus> allowedTransitions() {
            return Set.of();
        }
    };

    public abstract Set<EventStatus> allowedTransitions();

    public boolean canTransitionTo(EventStatus target) {
        return allowedTransitions().contains(target);
    }
}
