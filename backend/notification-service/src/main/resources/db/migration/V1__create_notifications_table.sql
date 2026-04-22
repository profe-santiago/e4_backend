CREATE TABLE notifications (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL,
    type         VARCHAR(50) NOT NULL,
    subject      VARCHAR(255),
    message      TEXT        NOT NULL,
    status       VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    reference_id UUID,
    sent_at      TIMESTAMP,
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);
