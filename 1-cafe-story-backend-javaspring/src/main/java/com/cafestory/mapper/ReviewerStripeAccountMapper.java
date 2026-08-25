package com.cafestory.mapper;

import com.cafestory.dto.responseDTO.ReviewerStripeAccountResponseDTO;
import com.cafestory.entity.Reviewer;
import com.cafestory.entity.ReviewerStripeAccount;
import org.springframework.stereotype.Component;

@Component
public class ReviewerStripeAccountMapper {

    public ReviewerStripeAccountResponseDTO toResponse(Reviewer reviewer, ReviewerStripeAccount account) {
        ReviewerStripeAccountResponseDTO dto = new ReviewerStripeAccountResponseDTO();
        dto.setReviewerId(reviewer.getReviewerId());
        dto.setUserName(reviewer.getUser().getUserName());
        dto.setUserAvatar(reviewer.getUser().getUserAvatar());
        dto.setStripeAccountId(account.getStripeAccountId());
        dto.setOnboardingStatus(account.getOnboardingStatus());
        dto.setChargesEnabled(account.isChargesEnabled());
        dto.setPayoutsEnabled(account.isPayoutsEnabled());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }
}
