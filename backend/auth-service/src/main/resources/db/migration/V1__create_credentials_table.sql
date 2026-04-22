CREATE TABLE credentials (
    id            BIGSERIAL     PRIMARY KEY,
    user_id       UUID          NOT NULL UNIQUE,
    email         VARCHAR(255)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    role          VARCHAR(50)   NOT NULL DEFAULT 'BUYER',
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);
