package com.cafestory.dto.responseDTO;

import lombok.Data;

@Data
public class ReviewerConnectOnboardResponseDTO {

    private String onboardingUrl;
    private String stripeAccountId;
    private String onboardingStatus;
}
