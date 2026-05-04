-- ============================================================
-- 006 - Create processed_messages table (idempotency)
-- ============================================================
CREATE TABLE snackoverflow.processed_messages
(
    message_id   VARCHAR(36)  PRIMARY KEY,
    message_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_processed_messages_at ON snackoverflow.processed_messages (processed_at);

