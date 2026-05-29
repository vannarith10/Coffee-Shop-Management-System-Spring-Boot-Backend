
-- Add email
ALTER TABLE users ADD COLUMN email VARCHAR(255) UNIQUE;

-- Add provider
ALTER TABLE users ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'local';

-- Add provider id
ALTER TABLE users ADD COLUMN provider_id VARCHAR(255);



-- Make username nullable because OAuth2 users won't have one
ALTER TABLE users ALTER COLUMN username DROP NOT NULL;

-- Make password nullable because OAuth2 users won't have one
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;