package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.ReviewerBadgeThresholdRequest;
import com.cafestory.dto.responseDTO.ReviewerBadgeThresholdResponseDTO;
import com.cafestory.entity.enums.ReviewerBadge;

import java.util.List;
import java.util.UUID;

public interface ReviewerBadgeThresholdService {

    ReviewerBadge badgeForScore(long score);

    List<ReviewerBadgeThresholdResponseDTO> getThresholds(UUID formulaId);

    List<ReviewerBadgeThresholdResponseDTO> updateThresholds(UUID adminUserId, UUID formulaId, List<ReviewerBadgeThresholdRequest> thresholds);
}
