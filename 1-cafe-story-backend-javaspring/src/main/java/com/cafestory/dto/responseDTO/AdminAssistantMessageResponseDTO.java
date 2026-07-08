package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.AdminAssistantMessageRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class AdminAssistantMessageResponseDTO {
    private UUID id;
    private UUID conversationId;
    private AdminAssistantMessageRole role;
    private String content;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
