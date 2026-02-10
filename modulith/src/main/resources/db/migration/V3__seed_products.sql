INSERT INTO products (sku, name, price, stock_quantity)
VALUES ('LAPTOP-001', 'Laptop Pro 14', 1499.00, 25),
       ('MOUSE-001', 'Wireless Mouse', 39.90, 120),
       ('KEYBOARD-001', 'Mechanical Keyboard', 119.00, 60) ON DUPLICATE KEY
UPDATE name = VALUES(name), price = VALUES(price), stock_quantity = VALUES(stock_quantity);
