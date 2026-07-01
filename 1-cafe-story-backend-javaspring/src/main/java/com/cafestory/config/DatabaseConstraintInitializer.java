package com.cafestory.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

@Component
public class DatabaseConstraintInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseConstraintInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws SQLException {
        if (!isPostgreSQL()) {
            return;
        }

        syncNotificationTypeConstraint();
        syncChatCafePageActorSchema();
    }

    private boolean isPostgreSQL() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            return productName != null
                    && productName.toLowerCase(Locale.ROOT).contains("postgresql");
        }
    }

    private void syncNotificationTypeConstraint() {
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                    IF to_regclass('public.notifications') IS NOT NULL THEN
                        ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;
                        ALTER TABLE notifications
                            ADD CONSTRAINT notifications_type_check
                            CHECK (type IN ('LIKE', 'SHARE', 'COMMENT', 'MESSAGE', 'FOLLOW', 'TAG', 'BLOG_MODERATION'));
                    END IF;
                END $$;
                """);
    }

    private void syncChatCafePageActorSchema() {
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                    IF to_regclass('public.conversations') IS NOT NULL THEN
                        ALTER TABLE conversations
                            ADD COLUMN IF NOT EXISTS cafe_page_id uuid;

                        ALTER TABLE conversations DROP CONSTRAINT IF EXISTS conversations_type_check;
                        ALTER TABLE conversations
                            ADD CONSTRAINT conversations_type_check
                            CHECK (type IN ('DIRECT', 'GROUP', 'CAFE_PAGE'));

                        IF to_regclass('public.cafe_pages') IS NOT NULL
                                AND NOT EXISTS (
                                    SELECT 1
                                    FROM pg_constraint
                                    WHERE conname = 'fk_conversations_cafe_page'
                                ) THEN
                            ALTER TABLE conversations
                                ADD CONSTRAINT fk_conversations_cafe_page
                                FOREIGN KEY (cafe_page_id)
                                REFERENCES cafe_pages(id);
                        END IF;

                        CREATE INDEX IF NOT EXISTS idx_conversations_type_page
                            ON conversations(type, cafe_page_id);
                        CREATE INDEX IF NOT EXISTS idx_conversations_updated_at
                            ON conversations(updated_at);
                    END IF;

                    IF to_regclass('public.chat_messages') IS NOT NULL THEN
                        ALTER TABLE chat_messages
                            ADD COLUMN IF NOT EXISTS sender_context_type varchar(40) NOT NULL DEFAULT 'USER',
                            ADD COLUMN IF NOT EXISTS sender_cafe_page_id uuid;

                        IF to_regclass('public.cafe_pages') IS NOT NULL
                                AND NOT EXISTS (
                                    SELECT 1
                                    FROM pg_constraint
                                    WHERE conname = 'fk_chat_messages_sender_cafe_page'
                                ) THEN
                            ALTER TABLE chat_messages
                                ADD CONSTRAINT fk_chat_messages_sender_cafe_page
                                FOREIGN KEY (sender_cafe_page_id)
                                REFERENCES cafe_pages(id);
                        END IF;

                        CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_context_page
                            ON chat_messages(sender_context_type, sender_cafe_page_id);
                    END IF;
                END $$
                """);
    }
}
