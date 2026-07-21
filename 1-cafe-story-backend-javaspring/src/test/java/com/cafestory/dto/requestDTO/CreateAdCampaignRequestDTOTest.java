package com.cafestory.dto.requestDTO;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreateAdCampaignRequestDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validation_success_acceptsOptionalHttpUrls_TC001() {
        CreateAdCampaignRequestDTO request = validRequest();
        request.setImageUrl("https://cdn.example.com/ad.jpg");
        request.setTargetUrl("http://localhost:3000/cafes/bean-house");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void validation_fail_rejectsInvalidUrlsAndOversizedDescription_TC002() {
        CreateAdCampaignRequestDTO request = validRequest();
        request.setImageUrl("javascript:alert(1)");
        request.setTargetUrl("/cafes/relative");
        request.setDescription("x".repeat(1001));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("imageUrl", "targetUrl", "description");
    }

    private CreateAdCampaignRequestDTO validRequest() {
        CreateAdCampaignRequestDTO request = new CreateAdCampaignRequestDTO();
        request.setPaymentId(UUID.randomUUID());
        request.setCafePageId(UUID.randomUUID());
        request.setTitle("E2E CafeStory campaign");
        return request;
    }
}
