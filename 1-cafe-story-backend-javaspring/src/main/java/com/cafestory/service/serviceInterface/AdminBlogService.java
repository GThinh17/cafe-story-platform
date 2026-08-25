package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminPostStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminBlogService {

    Page<BlogResponseDTO> getBlogs(PostStatus status, UUID authorUserId, UUID pageId, Pageable pageable);

    BlogResponseDTO getBlog(UUID blogId);

    BlogResponseDTO updateBlogStatus(UUID blogId, AdminPostStatusUpdateRequestDTO request);

    void deleteBlog(UUID blogId);
}
