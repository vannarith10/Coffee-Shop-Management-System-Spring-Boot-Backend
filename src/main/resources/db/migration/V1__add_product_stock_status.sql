ALTER TABLE products
    ADD COLUMN stock_status VARCHAR(20);


UPDATE products
SET stock_status = CASE
    WHEN available = true THEN 'IN_STOCK'
    ELSE 'OUT_OF_STOCK'
END;


ALTER TABLE products
    ALTER COLUMN stock_status SET NOT NULL;