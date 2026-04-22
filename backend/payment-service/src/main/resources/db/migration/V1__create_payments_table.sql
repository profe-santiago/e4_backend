CREATE TABLE payments (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID          NOT NULL UNIQUE,
    user_id         UUID          NOT NULL,
    amount          NUMERIC(12,2) NOT NULL,
    currency        VARCHAR(10)   NOT NULL DEFAULT 'MXN',
    status          VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    payment_method  VARCHAR(50),
    transaction_id  VARCHAR(255)  UNIQUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);
