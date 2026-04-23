-- Agrega la columna price_currency al refactorizar TicketType.price como Value Object Money.
-- Default 'USD' para preservar datos existentes.
ALTER TABLE ticket_types
    ADD COLUMN IF NOT EXISTS price_currency VARCHAR(3) NOT NULL DEFAULT 'USD';
