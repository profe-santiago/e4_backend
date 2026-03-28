CREATE TABLE tickets (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id  BIGINT       NOT NULL REFERENCES order_items(id),
    user_id        UUID         NOT NULL,
    event_id       UUID         NOT NULL,
    ticket_type_id BIGINT       NOT NULL,
    qr_code        VARCHAR(500) NOT NULL UNIQUE,
    status         VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    purchased_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    used_at        TIMESTAMP
);
