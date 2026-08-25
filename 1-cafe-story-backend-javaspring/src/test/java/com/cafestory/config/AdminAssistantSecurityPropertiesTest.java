package com.cafestory.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminAssistantSecurityPropertiesTest {

    @Test
    void trimsConfiguredToolToken() {
        AdminAssistantSecurityProperties properties =
                new AdminAssistantSecurityProperties("  secure-local-token  ");

        assertThat(properties.getToolToken()).isEqualTo("secure-local-token");
    }

    @Test
    void rejectsBlankToolToken() {
        assertThatThrownBy(() -> new AdminAssistantSecurityProperties(" "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_ASSISTANT_TOOL_TOKEN");
    }

    @Test
    void rejectsDevDefaultToolToken() {
        assertThatThrownBy(() -> new AdminAssistantSecurityProperties("cafestory-dev-assistant-tool-token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ADMIN_ASSISTANT_TOOL_TOKEN");
    }
}
