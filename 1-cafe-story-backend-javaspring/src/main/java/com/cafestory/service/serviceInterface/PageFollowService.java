package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.PageFollowResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PageFollowService {
    PageFollowResponseDTO followPage(UUID cafePageId, UUID userId);

    void unfollowPage(UUID cafePageId, UUID userId);

    List<PageFollowResponseDTO> getFollowersByCafePageId(UUID cafePageId);

    List<PageFollowResponseDTO> getFollowedPagesByUserId(UUID userId);
}
