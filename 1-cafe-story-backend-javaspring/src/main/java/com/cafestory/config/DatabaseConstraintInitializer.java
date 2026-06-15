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
                            CHECK (type IN ('LIKE', 'SHARE', 'COMMENT', 'MESSAGE', 'FOLLOW', 'TAG'));
                    END IF;
                END $$;
                """);
    }
}
