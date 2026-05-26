package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminPostStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.repository.BlogRepository;
import com.cafestory.service.serviceInterface.AdminBlogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AdminBlogServiceImpl implements AdminBlogService {

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;

    public AdminBlogServiceImpl(BlogRepository blogRepository, BlogMapper blogMapper) {
        this.blogRepository = blogRepository;
        this.blogMapper = blogMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponseDTO> getBlogs(PostStatus status, UUID authorUserId, UUID pageId, Pageable pageable) {
        return blogRepository.findAdminBlogs(status, authorUserId, pageId, pageable)
                .map(blogMapper::toBlogResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogResponseDTO getBlog(UUID blogId) {
        return blogMapper.toBlogResponseDTO(findBlog(blogId));
    }

    @Override
    @Transactional
    public BlogResponseDTO updateBlogStatus(UUID blogId, AdminPostStatusUpdateRequestDTO request) {
        Blog blog = findBlog(blogId);
        blog.setStatus(request.getStatus());
        return blogMapper.toBlogResponseDTO(blogRepository.save(blog));
    }

    @Override
    @Transactional
    public void deleteBlog(UUID blogId) {
        Blog blog = findBlog(blogId);
        blogRepository.delete(blog);
    }

    private Blog findBlog(UUID blogId) {
        return blogRepository.findById(blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));
    }
}
