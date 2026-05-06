
-- 1. Add Shift Type to users table
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS shift_type VARCHAR(10);


-- 2. Backfill existing users (required because nullable = false)
UPDATE users
    SET shift_type = 'FULL_DAY'
    WHERE shift_type IS NULL;


-- 3. Enforce NOT NULL
ALTER TABLE users
    ALTER COLUMN shift_type SET NOT NULL;


-- 4. Create Schedule Table (one row per day per user)
CREATE TABLE IF NOT EXISTS user_schedules (
    user_id UUID NOT NULL,
    schedule_day VARCHAR(10) NOT NULL,
    CONSTRAINT fk_user_schedules_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


-- 5. Prevent duplicate days for same user
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_schedule_day
    ON user_schedules(user_id, schedule_day);
