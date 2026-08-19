package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.UUID;

@Data
public class AdminContentTranslationResponseDTO {
    private UUID requestId;
    private String detectedLocale;
    private String targetLocale;
    private String translatedText;
    private String translationState;
    private String modelName;
}
