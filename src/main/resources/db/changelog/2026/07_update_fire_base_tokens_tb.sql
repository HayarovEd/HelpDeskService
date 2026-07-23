-- Update firebase_tokens table
ALTER TABLE firebase_tokens
    ADD COLUMN platform VARCHAR(10) NOT NULL DEFAULT 'FCM',
    ADD COLUMN device_id VARCHAR(255),
    ADD COLUMN device_name VARCHAR(100),
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE firebase_tokens
    ALTER COLUMN token TYPE TEXT;

-- Remove the default value from the platform column after updating existing rows
ALTER TABLE firebase_tokens
    ALTER COLUMN platform DROP DEFAULT;
