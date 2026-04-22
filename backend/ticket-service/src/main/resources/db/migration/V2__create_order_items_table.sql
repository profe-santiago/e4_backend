CREATE TABLE order_items (
    id             BIGSERIAL     PRIMARY KEY,
    order_id       UUID          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    event_id       UUID          NOT NULL,
    ticket_type_id BIGINT        NOT NULL,
    quantity       INTEGER       NOT NULL,
    unit_price     NUMERIC(10,2) NOT NULL
);
