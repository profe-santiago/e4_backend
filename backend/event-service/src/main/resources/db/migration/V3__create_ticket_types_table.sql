CREATE TABLE ticket_types (
    id                 BIGSERIAL     PRIMARY KEY,
    event_id           UUID          NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    name               VARCHAR(100)  NOT NULL,
    description        VARCHAR(500),
    price              NUMERIC(10,2) NOT NULL,
    total_quantity     INTEGER       NOT NULL,
    available_quantity INTEGER       NOT NULL
);
