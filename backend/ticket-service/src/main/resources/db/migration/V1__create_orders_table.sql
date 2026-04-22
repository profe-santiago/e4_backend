CREATE TABLE orders (
    id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID          NOT NULL,
    status       VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    total_amount NUMERIC(12,2) NOT NULL,
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP
);
