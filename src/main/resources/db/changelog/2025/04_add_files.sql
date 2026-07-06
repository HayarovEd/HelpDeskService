-- changeset Khayarov Edward:040720261552-add-files-table-v3
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'files'

CREATE TABLE files (
    id UUID PRIMARY KEY,
    original_file_name VARCHAR(255),
    stored_file_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(255),
    created_date TIMESTAMP,
    message_id UUID NOT NULL,
    CONSTRAINT fk_files_message
        FOREIGN KEY(message_id)
        REFERENCES messages(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_files_message_id ON files(message_id);
