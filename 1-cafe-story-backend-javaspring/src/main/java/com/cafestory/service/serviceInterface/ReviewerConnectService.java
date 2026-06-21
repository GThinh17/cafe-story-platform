package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.ReviewerConnectOnboardResponseDTO;
import com.cafestory.dto.responseDTO.ReviewerStripeAccountResponseDTO;

import java.util.UUID;

public interface ReviewerConnectService {

    ReviewerConnectOnboardResponseDTO createOnboardingLink(UUID userId);

    ReviewerConnectOnboardResponseDTO refreshOnboardingLink(UUID userId);

    void handleAccountUpdated(String stripeAccountId, boolean chargesEnabled, boolean payoutsEnabled);

    ReviewerStripeAccountResponseDTO getStatus(UUID userId);

    ReviewerStripeAccountResponseDTO sync(UUID userId);
}
