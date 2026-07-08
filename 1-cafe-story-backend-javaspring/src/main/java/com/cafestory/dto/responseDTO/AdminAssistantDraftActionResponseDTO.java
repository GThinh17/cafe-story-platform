package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.AdminAssistantDraftActionStatus;
import com.cafestory.entity.enums.AdminAssistantDraftActionType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class AdminAssistantDraftActionResponseDTO {
    private UUID id;
    private UUID conversationId;
    private UUID messageId;
    private AdminAssistantDraftActionType actionType;
    private Map<String, Object> payload;
    private String explanation;
    private List<Map<String, Object>> sourceRefs;
    private AdminAssistantDraftActionStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime executedAt;
    private Map<String, Object> executionResult;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
