package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.PageLikeResponseDTO;

import java.util.List;
import java.util.UUID;

public interface PageLikeService {
    PageLikeResponseDTO likePage(UUID cafePageId, UUID userId);

    void unlikePage(UUID cafePageId, UUID userId);

    List<PageLikeResponseDTO> getLikesByCafePageId(UUID cafePageId);

    List<PageLikeResponseDTO> getLikesByUserId(UUID userId);
}
