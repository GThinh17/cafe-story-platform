package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.BlogTaggedUserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BlogService {
    BlogResponseDTO createBlog(BlogCreateDTO blogCreateDTO, UUID actorUserId);

    List<BlogResponseDTO> getAllBlogs();

    List<BlogResponseDTO> getAllBlogs(UUID viewerUserId);

    List<BlogResponseDTO> getBlogsByAuthorId(UUID authorUserId);

    List<BlogResponseDTO> getAllBlogsByUserId(UUID userId);

    List<BlogResponseDTO> getAllBlogsByUserId(UUID userId, UUID viewerUserId);

    BlogResponseDTO getBlogById(UUID blogId);

    BlogResponseDTO getBlogById(UUID blogId, UUID viewerUserId);

    BlogResponseDTO updateBlog(UUID blogId, UUID actorUserId, BlogUpdateDTO blogUpdateDTO);

    void deleteBlog(UUID blogId, UUID actorUserId);

    List<BlogTaggedUserResponseDTO> getTagSuggestions(UUID actorUserId, String keyword);
}
