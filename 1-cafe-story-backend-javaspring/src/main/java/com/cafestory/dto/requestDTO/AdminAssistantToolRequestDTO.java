package com.cafestory.dto.requestDTO;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class AdminAssistantToolRequestDTO {
    private UUID conversationId;
    private UUID messageId;
    private UUID adminUserId;
    private Map<String, Object> input;
}
