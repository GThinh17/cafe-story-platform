package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.dto.responseDTO.BlogTaggedUserResponseDTO;
import com.cafestory.entity.enums.PostStatus;

import java.util.List;
import java.util.UUID;

public interface BlogService {
    BlogResponseDTO createBlog(BlogCreateDTO blogCreateDTO, UUID actorUserId);

    BlogResponseDTO createModeratedBlog(BlogCreateDTO blogCreateDTO, UUID actorUserId);

    List<BlogResponseDTO> getAllBlogs();

    List<BlogResponseDTO> getAllBlogs(UUID viewerUserId);

    List<BlogResponseDTO> getBlogsByAuthorId(UUID authorUserId);

    List<BlogResponseDTO> getAllBlogsByUserId(UUID userId);

    List<BlogResponseDTO> getAllBlogsByUserId(UUID userId, UUID viewerUserId);

    List<BlogResponseDTO> getAllBlogsByUserId(UUID userId, UUID viewerUserId, PostStatus status);

    List<BlogResponseDTO> getSavedBlogsByUserId(UUID userId, UUID viewerUserId);

    List<BlogResponseDTO> getSharedBlogsByUserId(UUID userId, UUID viewerUserId);

    List<BlogResponseDTO> getSharedBlogsByUserId(UUID userId, UUID viewerUserId, String sort);

    List<BlogResponseDTO> getTaggedBlogsByUserId(UUID userId, UUID viewerUserId);

    BlogResponseDTO getBlogById(UUID blogId);

    BlogResponseDTO getBlogById(UUID blogId, UUID viewerUserId);

    BlogResponseDTO updateBlog(UUID blogId, UUID actorUserId, BlogUpdateDTO blogUpdateDTO);

    void deleteBlog(UUID blogId, UUID actorUserId);

    List<BlogTaggedUserResponseDTO> getTagSuggestions(UUID actorUserId, String keyword);
}
