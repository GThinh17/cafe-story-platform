package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AdminAssistantConversationResponseDTO {
    private UUID id;
    private UUID adminUserId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
