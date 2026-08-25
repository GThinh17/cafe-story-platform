package com.cafestory.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresIntegrationTestSupport.InMemoryCacheConfiguration.class)
abstract class PostgresIntegrationTestSupport {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cafestory_it")
            .withUsername("cafestory")
            .withPassword("cafestory");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.cache.type", () -> "redis");
        registry.add("spring.data.redis.repositories.enabled", () -> "false");
        registry.add("app.data-initializer.enabled", () -> "false");
        registry.add("app.scheduling.enabled", () -> "false");
        registry.add("app.jwt.secret", () -> "cafestory-test-jwt-secret-change-me-at-least-32-bytes");
        registry.add("app.jwt.access-token-seconds", () -> "900");
        registry.add("app.jwt.refresh-token-seconds", () -> "604800");
        registry.add("app.auth.cookie.secure", () -> "false");
        registry.add("app.auth.cookie.same-site", () -> "Lax");
        registry.add("rag.internal.secret", () -> "cafestory-test-rag-secret");
        registry.add("rag.internal.timestamp-window-seconds", () -> "300");
        registry.add("stripe.secret-key", () -> "sk_test_not_used");
        registry.add("stripe.webhook-secret", () -> "whsec_test_not_used");
        registry.add("stripe.connect-webhook-secret", () -> "whsec_connect_test_not_used");
        registry.add("vnpay.tmn-code", () -> "CAFESTORY");
        registry.add("vnpay.hash-secret", () -> "cafestory-vnpay-test-secret");
        registry.add("vnpay.pay-url", () -> "https://example.invalid/vnpay");
        registry.add("vnpay.return-url", () -> "http://localhost/api/payments/vnpay/return");
        registry.add("vnpay.ipn-url", () -> "http://localhost/api/payments/vnpay/ipn");
        registry.add("ai.moderation.base-url", () -> "http://localhost:1");
        registry.add("report.moderation.webhook-url", () -> System.getProperty(
                "cafestory.it.reportModerationWebhookUrl",
                "http://localhost:1/report-moderation"));
        registry.add("admin.report.ai.webhook-url", () -> "http://localhost:1/admin-report-ai");
        registry.add("admin.report.ai.hmac-secret", () -> "cafestory-admin-report-ai-test-secret");
        registry.add("admin.assistant.webhook-url", () -> "http://localhost:1/admin-assistant");
        registry.add("admin.assistant.tool-token", () -> "cafestory-test-assistant-tool-token");
        registry.add("rag.python.base-url", () -> "http://localhost:1");
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("""
                DO $$
                DECLARE
                    statement text;
                BEGIN
                    FOR statement IN
                        SELECT 'TRUNCATE TABLE ' || string_agg(format('%I.%I', schemaname, tablename), ', ')
                            || ' RESTART IDENTITY CASCADE'
                        FROM pg_tables
                        WHERE schemaname = 'public'
                    LOOP
                        IF statement IS NOT NULL THEN
                            EXECUTE statement;
                        END IF;
                    END LOOP;
                END $$;
                """);
    }

    @TestConfiguration
    static class InMemoryCacheConfiguration {

        @Bean
        @Primary
        CacheManager integrationTestCacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }
}
