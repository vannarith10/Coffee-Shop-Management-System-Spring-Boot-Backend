

-- 1. Add the new nullable foreign key column
ALTER TABLE users
    ADD COLUMN shop_profile_id UUID;

-- 2. Add the foreign key constraint
ALTER TABLE users
    ADD CONSTRAINT fk_users_shop_profile
        FOREIGN KEY (shop_profile_id)
            REFERENCES shop_profile(id)
            ON DELETE SET NULL;

-- 3. Create an index for faster joins and lookups
CREATE INDEX idx_users_shop_profile_id
    ON users(shop_profile_id);