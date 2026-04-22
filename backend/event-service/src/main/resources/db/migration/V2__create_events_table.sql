CREATE TABLE events (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    organizer_id UUID         NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    category_id  BIGINT       REFERENCES categories(id) ON DELETE SET NULL,
    venue        VARCHAR(255) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    country      VARCHAR(100) NOT NULL,
    start_date   TIMESTAMP    NOT NULL,
    end_date     TIMESTAMP,
    image_url    VARCHAR(500),
    status       VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP
);
