package com.cafestory.dto.requestDTO;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class AdminContentTranslationWebhookRequestDTO {
    String contractVersion;
    UUID requestId;
    String text;
    String targetLocale;
    String contentKind;
}
