package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.ExploreSearchResponseDTO;

public interface ExploreSearchService {
    ExploreSearchResponseDTO search(String query, int size);
}
