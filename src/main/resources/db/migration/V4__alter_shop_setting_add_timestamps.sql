-- Add timestamp columns
ALTER TABLE shop_settings
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

-- Populate existing rows with current timestamp
UPDATE shop_settings
SET created_at = NOW(),
    updated_at = NOW()
WHERE created_at IS NULL;

-- Make non-nullable
ALTER TABLE shop_settings
    ALTER COLUMN created_at SET NOT NULL,
ALTER COLUMN updated_at SET NOT NULL;

-- Set defaults
ALTER TABLE shop_settings
    ALTER COLUMN created_at SET DEFAULT NOW(),
ALTER COLUMN updated_at SET DEFAULT NOW();