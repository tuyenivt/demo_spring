CREATE TABLE IF NOT EXISTS product
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
    in_stock BIGINT,
    date_of_manufacture DATETIME,
    updated_at DATETIME,
    vendor VARCHAR
(
    255
)
    );

CREATE INDEX idx_product_updated_at ON product (updated_at);
