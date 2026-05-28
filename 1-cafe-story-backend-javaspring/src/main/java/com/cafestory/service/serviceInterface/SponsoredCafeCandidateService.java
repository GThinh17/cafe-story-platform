package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.SponsoredCafeResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SponsoredCafeCandidateService {
    List<SponsoredCafeResponseDTO> getCandidates(UUID userId, int requestedCount, int adOffset);
}
