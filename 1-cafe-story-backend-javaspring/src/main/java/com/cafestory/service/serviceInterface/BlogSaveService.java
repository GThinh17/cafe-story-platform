package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.BlogSaveResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BlogSaveService {
    BlogSaveResponseDTO saveBlog(UUID blogId, UUID userId);

    void unsaveBlog(UUID blogId, UUID userId);

    List<BlogSaveResponseDTO> getSavesByBlogId(UUID blogId);

    List<BlogSaveResponseDTO> getSavesByUserId(UUID userId);
}
