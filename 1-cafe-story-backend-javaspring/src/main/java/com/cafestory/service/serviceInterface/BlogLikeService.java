package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.BlogLikeResponseDTO;
import com.cafestory.entity.enums.ActorContextType;

import java.util.List;
import java.util.UUID;

public interface BlogLikeService {
    BlogLikeResponseDTO likeBlog(UUID blogId, UUID userId);

    BlogLikeResponseDTO likeBlog(UUID blogId, UUID userId, ActorContextType actorContextType, UUID actorCafePageId);

    void unlikeBlog(UUID blogId, UUID userId);

    void unlikeBlog(UUID blogId, UUID userId, ActorContextType actorContextType, UUID actorCafePageId);

    List<BlogLikeResponseDTO> getLikesByBlogId(UUID blogId);

    List<BlogLikeResponseDTO> getLikesByUserId(UUID userId);
}
