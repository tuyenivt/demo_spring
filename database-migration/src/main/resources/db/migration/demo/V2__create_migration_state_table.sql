CREATE TABLE IF NOT EXISTS migration_state
(
    entity_name
    VARCHAR
(
    100
) PRIMARY KEY,
    last_updated_at DATETIME NOT NULL,
    updated_by VARCHAR
(
    100
),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );
