CREATE TABLE message_sources (
    id BINARY(16) PRIMARY KEY,
    message_id BINARY(16) NOT NULL,
    title VARCHAR(255) NOT NULL,
    link VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    INDEX idx_message_sources_message_id (message_id)
);