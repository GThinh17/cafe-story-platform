package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.RagTopReviewerResponseDTO;
import com.cafestory.dto.responseDTO.RagTrendingCafeResponseDTO;

import java.util.List;

public interface RagToolService {

    List<RagTrendingCafeResponseDTO> getTrendingCafes(String province, int limit);

    List<RagTopReviewerResponseDTO> getTopReviewers(int limit);
}
