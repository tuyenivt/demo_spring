CREATE TABLE IF NOT EXISTS event_publication (
    id CHAR(36) PRIMARY KEY,
    listener_id VARCHAR(512) NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    serialized_event CLOB NOT NULL,
    publication_date TIMESTAMP NOT NULL,
    completion_date TIMESTAMP NULL
);
