-- Create firebase_tokens table
CREATE TABLE firebase_tokens (
                       id UUID PRIMARY KEY,
                       token VARCHAR(255),
                       user_id UUID NOT NULL,
                       CONSTRAINT fk_firebase_tokens_user
                           FOREIGN KEY(user_id)
                               REFERENCES users(id)
                               ON DELETE CASCADE
);
