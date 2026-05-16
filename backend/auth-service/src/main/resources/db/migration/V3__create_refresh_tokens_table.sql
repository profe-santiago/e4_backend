CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    credential_id BIGINT NOT NULL REFERENCES credentials(id) ON DELETE CASCADE,
    token       VARCHAR(36) NOT NULL UNIQUE,
    expires_at  TIMESTAMP   NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
