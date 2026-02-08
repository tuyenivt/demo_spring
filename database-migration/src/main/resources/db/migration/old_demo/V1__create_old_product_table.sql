CREATE TABLE IF NOT EXISTS old_product
(
    product_id
    BIGINT
    PRIMARY
    KEY,
    product_name
    VARCHAR
(
    255
),
    price DECIMAL
(
    10,
    2
),
    quality BIGINT,
    date_of_manufacture DATETIME,
    updated_at DATETIME
    );

CREATE INDEX idx_old_product_updated_at ON old_product (updated_at);
