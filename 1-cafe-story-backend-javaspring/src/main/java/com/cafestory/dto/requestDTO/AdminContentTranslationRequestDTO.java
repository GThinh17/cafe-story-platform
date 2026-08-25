package com.cafestory.dto.requestDTO;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class AdminContentTranslationRequestDTO {

    public static final Set<String> ALLOWED_TARGET_LOCALES = Set.of("en", "vi");
    public static final Set<String> ALLOWED_CONTENT_KINDS = Set.of(
            "BLOG_CONTENT",
            "COMMENT_CONTENT",
            "REPORT_DESCRIPTION",
            "REPORT_REASON",
            "AI_EXPLANATION",
            "AI_RATIONALE",
            "MODERATION_REASON");

    @NotBlank(message = "text is required")
    @Size(max = 20_000, message = "text must not exceed 20000 characters")
    private String text;

    @NotBlank(message = "targetLocale is required")
    private String targetLocale;

    @NotBlank(message = "contentKind is required")
    private String contentKind;

    @AssertTrue(message = "targetLocale must be en or vi")
    public boolean isTargetLocaleValid() {
        return targetLocale != null && ALLOWED_TARGET_LOCALES.contains(targetLocale);
    }

    @AssertTrue(message = "contentKind is not supported")
    public boolean isContentKindValid() {
        return contentKind != null && ALLOWED_CONTENT_KINDS.contains(contentKind);
    }
}
