package com.cafestory.entity;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class AdminReportAiResolutionSchemaContractTest {

    private static final String SNAPSHOT_HASH_MIGRATION =
            "db/migration/V20260730_01__admin_report_ai_snapshot_hash_length.sql";

    @Test
    void targetSnapshotHash_entityAndMigrationFitCanonicalSha256Value() throws Exception {
        String canonicalHash = "sha256:" + "a".repeat(64);
        Field field = AdminReportAiResolution.class.getDeclaredField("targetSnapshotHash");
        Column column = field.getAnnotation(Column.class);

        assertThat(canonicalHash).hasSize(71);
        assertThat(column).isNotNull();
        assertThat(column.name()).isEqualTo("target_snapshot_hash");
        assertThat(column.length()).isEqualTo(canonicalHash.length());
        assertThat(readClasspathResource(SNAPSHOT_HASH_MIGRATION))
                .contains("ALTER TABLE admin_report_ai_resolutions")
                .contains("ALTER COLUMN target_snapshot_hash TYPE VARCHAR(71)");
    }

    private String readClasspathResource(String resourcePath) throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertThat(input).as("classpath resource %s", resourcePath).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
