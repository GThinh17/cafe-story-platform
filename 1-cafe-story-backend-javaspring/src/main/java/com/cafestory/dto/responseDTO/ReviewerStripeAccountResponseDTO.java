package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewerStripeAccountResponseDTO {

    private UUID reviewerId;
    private String userName;
    private String userAvatar;
    private String stripeAccountId;
    private String onboardingStatus;
    private boolean chargesEnabled;
    private boolean payoutsEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
