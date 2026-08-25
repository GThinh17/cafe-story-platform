package com.cafestory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminAssistantSecurityProperties {

    private static final String DEV_TOOL_TOKEN = "cafestory-dev-assistant-tool-token";

    private final String toolToken;

    public AdminAssistantSecurityProperties(
            @Value("${admin.assistant.tool-token:}") String toolToken) {
        this.toolToken = validateToolToken(toolToken);
    }

    public String getToolToken() {
        return toolToken;
    }

    private String validateToolToken(String value) {
        if (value == null || value.isBlank() || DEV_TOOL_TOKEN.equals(value.trim())) {
            throw new IllegalStateException(
                    "ADMIN_ASSISTANT_TOOL_TOKEN must be configured with a non-default secret");
        }
        return value.trim();
    }
}
