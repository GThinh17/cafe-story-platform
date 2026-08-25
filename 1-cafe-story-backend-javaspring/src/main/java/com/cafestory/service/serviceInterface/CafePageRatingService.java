package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.CafePageRatingResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CafePageRatingService {
    CafePageRatingResponseDTO rateCafePage(UUID cafePageId, UUID userId, Integer rating);

    void deleteRating(UUID cafePageId, UUID userId);

    List<CafePageRatingResponseDTO> getRatingsByCafePageId(UUID cafePageId);

    List<CafePageRatingResponseDTO> getRatingsByUserId(UUID userId);
}
