package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.FeedImpressionBatchRequestDTO;
import com.cafestory.dto.responseDTO.FeedImpressionResponseDTO;

import java.util.UUID;

public interface FeedImpressionService {
    FeedImpressionResponseDTO recordImpressions(UUID userId, FeedImpressionBatchRequestDTO request);
}
