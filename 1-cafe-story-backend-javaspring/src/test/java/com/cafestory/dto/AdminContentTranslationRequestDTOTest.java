package com.cafestory.dto;

import com.cafestory.dto.requestDTO.AdminContentTranslationRequestDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminContentTranslationRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(validator.validate(request("Text", "vi", "BLOG_CONTENT"))).isEmpty();
        assertThat(validator.validate(request("Text", "en", "AI_RATIONALE"))).isEmpty();
    }

    @Test
    void invalidRequest_rejectsBlankOversizeLocaleAndContentKind() {
        assertThat(validator.validate(request(" ", "fr", "UNKNOWN")))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("text", "targetLocaleValid", "contentKindValid");
        assertThat(validator.validate(request("x".repeat(20_001), null, null)))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("text", "targetLocale", "contentKind", "targetLocaleValid", "contentKindValid");
    }

    private AdminContentTranslationRequestDTO request(String text, String locale, String kind) {
        AdminContentTranslationRequestDTO request = new AdminContentTranslationRequestDTO();
        request.setText(text);
        request.setTargetLocale(locale);
        request.setContentKind(kind);
        return request;
    }
}
