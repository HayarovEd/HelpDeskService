-- Create a new ENUM type for platform
CREATE TYPE platform_enum AS ENUM ('FCM', 'HMS');

-- Alter the firebase_tokens table to use the new enum type
ALTER TABLE firebase_tokens
    ALTER COLUMN platform TYPE platform_enum USING platform::platform_enum;
