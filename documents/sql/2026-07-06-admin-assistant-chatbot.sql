CREATE TABLE IF NOT EXISTS admin_assistant_conversations (
    id UUID PRIMARY KEY,
    admin_user_id UUID NOT NULL,
    title VARCHAR(160),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_admin_assistant_conversations_admin_updated
    ON admin_assistant_conversations(admin_user_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS admin_assistant_messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    role VARCHAR(24) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_admin_assistant_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES admin_assistant_conversations(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_admin_assistant_messages_conversation_created
    ON admin_assistant_messages(conversation_id, created_at);

CREATE TABLE IF NOT EXISTS admin_assistant_tool_calls (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    message_id UUID,
    tool_name VARCHAR(100) NOT NULL,
    input_payload JSONB,
    output_summary JSONB,
    status VARCHAR(24) NOT NULL,
    error_message TEXT,
    duration_ms BIGINT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_admin_assistant_tool_calls_conversation
        FOREIGN KEY (conversation_id) REFERENCES admin_assistant_conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_assistant_tool_calls_message
        FOREIGN KEY (message_id) REFERENCES admin_assistant_messages(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_assistant_tool_calls_conversation_created
    ON admin_assistant_tool_calls(conversation_id, created_at);

CREATE INDEX IF NOT EXISTS idx_admin_assistant_tool_calls_tool_created
    ON admin_assistant_tool_calls(tool_name, created_at);

CREATE TABLE IF NOT EXISTS admin_assistant_draft_actions (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    message_id UUID,
    admin_user_id UUID NOT NULL,
    action_type VARCHAR(64) NOT NULL,
    payload JSONB,
    explanation TEXT,
    source_refs JSONB,
    status VARCHAR(24) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    executed_at TIMESTAMP,
    executed_by_admin_user_id UUID,
    execution_result JSONB,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_admin_assistant_draft_actions_conversation
        FOREIGN KEY (conversation_id) REFERENCES admin_assistant_conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_assistant_draft_actions_message
        FOREIGN KEY (message_id) REFERENCES admin_assistant_messages(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_admin_assistant_draft_actions_conversation_created
    ON admin_assistant_draft_actions(conversation_id, created_at);

CREATE INDEX IF NOT EXISTS idx_admin_assistant_draft_actions_status_expires
    ON admin_assistant_draft_actions(status, expires_at);
