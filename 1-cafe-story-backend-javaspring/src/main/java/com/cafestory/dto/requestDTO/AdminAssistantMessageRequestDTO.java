package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class AdminAssistantMessageRequestDTO {

    @NotBlank(message = "message is required")
    @Size(max = 4000, message = "message must be at most 4000 characters")
    private String message;

    private Map<String, Object> pageContext;
}
