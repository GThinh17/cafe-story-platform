package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.BlogTaggedUserResponseDTO;
import com.cafestory.entity.Blog;

import java.util.List;
import java.util.UUID;

public interface BlogTagService {
    List<BlogTaggedUserResponseDTO> syncBlogTags(Blog blog, UUID actorUserId, List<UUID> taggedUserIds);

    List<BlogTaggedUserResponseDTO> getTaggedUsers(UUID blogId);

    List<BlogTaggedUserResponseDTO> getTagSuggestions(UUID actorUserId, String keyword);

    void deleteBlogTags(UUID blogId);
}
