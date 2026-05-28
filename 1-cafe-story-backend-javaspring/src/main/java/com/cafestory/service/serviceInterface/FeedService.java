package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.FeedResponseDTO;

import java.util.UUID;

public interface FeedService {

    FeedResponseDTO getFeed(UUID userId, String cursor, int size);
}
