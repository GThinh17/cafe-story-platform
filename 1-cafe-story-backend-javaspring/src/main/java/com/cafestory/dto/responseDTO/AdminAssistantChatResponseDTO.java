package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AdminAssistantChatResponseDTO {
    private AdminAssistantMessageResponseDTO message;
    private AdminAssistantDraftActionResponseDTO draftAction;
    private List<Map<String, Object>> citations;
    private List<Map<String, Object>> toolCalls;
}
