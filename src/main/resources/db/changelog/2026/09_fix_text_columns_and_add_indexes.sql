-- Alter text columns to TEXT
ALTER TABLE messages ALTER COLUMN text TYPE TEXT;
ALTER TABLE logs ALTER COLUMN text TYPE TEXT;

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_connections_chat_id ON connections(chat_id);
CREATE INDEX IF NOT EXISTS idx_connections_user_id ON connections(user_id);
CREATE INDEX IF NOT EXISTS idx_messages_connection_id ON messages(connection_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_date ON messages(created_date);
CREATE INDEX IF NOT EXISTS idx_chats_group_id ON chats(group_id);
CREATE INDEX IF NOT EXISTS idx_chats_login ON chats(login);
CREATE INDEX IF NOT EXISTS idx_chats_created_date ON chats(created_date);
CREATE INDEX IF NOT EXISTS idx_firebase_tokens_user_id ON firebase_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_firebase_tokens_token ON firebase_tokens(token);
