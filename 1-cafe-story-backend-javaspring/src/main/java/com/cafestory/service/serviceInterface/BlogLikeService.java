package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.BlogLikeResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BlogLikeService {
    BlogLikeResponseDTO likeBlog(UUID blogId, UUID userId);

    void unlikeBlog(UUID blogId, UUID userId);

    List<BlogLikeResponseDTO> getLikesByBlogId(UUID blogId);

    List<BlogLikeResponseDTO> getLikesByUserId(UUID userId);
}
