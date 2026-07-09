package com.cafestory.dto.responseDTO;

import com.cafestory.entity.enums.AdminAssistantDraftActionType;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AdminAssistantWebhookResponseDTO {
    private String answer;
    private String modelName;
    private List<Map<String, Object>> citations;
    private List<Map<String, Object>> toolCalls;
    private DraftAction draftAction;

    @Data
    public static class DraftAction {
        private AdminAssistantDraftActionType actionType;
        private Map<String, Object> payload;
        private String explanation;
        private List<Map<String, Object>> sourceRefs;
    }
}
