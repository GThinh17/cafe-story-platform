package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BlogService {
    BlogResponseDTO createBlog(BlogCreateDTO blogCreateDTO, UUID actorUserId);

    List<BlogResponseDTO> getAllBlogs();

    List<BlogResponseDTO> getBlogsByAuthorId(UUID authorUserId);

    List<BlogResponseDTO> getAllBlogsByUserId(UUID userId);

    BlogResponseDTO getBlogById(UUID blogId);

    BlogResponseDTO updateBlog(UUID blogId, UUID actorUserId, BlogUpdateDTO blogUpdateDTO);

    void deleteBlog(UUID blogId, UUID actorUserId);
}
