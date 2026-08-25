package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class AdminAssistantToolResponseDTO {
    private String toolName;
    private Object data;
    private List<String> maskedFields;
    private List<Map<String, Object>> sourceRefs;
    private LocalDateTime generatedAt;
}
