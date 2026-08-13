package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.BlogShareResponseDTO;
import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.ShareType;

import java.util.List;
import java.util.UUID;

public interface BlogShareService {
    BlogShareResponseDTO shareBlog(UUID blogId, UUID userId, ShareType shareType);

    BlogShareResponseDTO shareBlog(UUID blogId, UUID userId, ShareType shareType, ActorContextType actorContextType, UUID actorCafePageId);

    void deleteShare(UUID blogId, UUID userId);

    List<BlogShareResponseDTO> getSharesByBlogId(UUID blogId);

    List<BlogShareResponseDTO> getSharesByUserId(UUID userId);
}
