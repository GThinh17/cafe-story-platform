package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.RagBlogModerationItemResponseDTO;

import java.util.List;

public interface RagUserContextService {

    List<RagBlogModerationItemResponseDTO> getBlogModerationForUser(String userJwt, Integer limit);
}
