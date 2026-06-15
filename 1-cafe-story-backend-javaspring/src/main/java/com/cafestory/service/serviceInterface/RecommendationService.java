package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.RecommendationCardResponseDTO;

import java.util.List;
import java.util.UUID;

public interface RecommendationService {
    List<RecommendationCardResponseDTO> getUserRecommendations(UUID currentUserId, int page, int size);

    List<RecommendationCardResponseDTO> getReviewerRecommendations(UUID currentUserId, int page, int size);

    List<RecommendationCardResponseDTO> getCafePageRecommendations(UUID currentUserId, int page, int size);

    List<RecommendationCardResponseDTO> getMixedRecommendations(UUID currentUserId, int page, int size);
}
