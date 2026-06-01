package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.BlogRatingResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BlogRatingService {
    BlogRatingResponseDTO rateBlog(UUID blogId, UUID userId, Integer rating);

    void deleteRating(UUID blogId, UUID userId);

    List<BlogRatingResponseDTO> getRatingsByBlogId(UUID blogId);

    List<BlogRatingResponseDTO> getRatingsByUserId(UUID userId);
}
