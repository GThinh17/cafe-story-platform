package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.User;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.BlogService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final BlogMapper blogMapper;

    public BlogServiceImpl(BlogRepository blogRepository, UserRepository userRepository, BlogMapper blogMapper) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
        this.blogMapper = blogMapper;
    }

    @Override
    @Transactional
    public BlogResponseDTO createBlog(BlogCreateDTO blogCreateDTO) {
        User author = findUserById(blogCreateDTO.getAuthorUserId());

        Blog blog = blogMapper.toBlog(blogCreateDTO);
        blog.setAuthor(author);
        if (blogCreateDTO.getIsPinned() != null) {
            blog.setIsPinned(blogCreateDTO.getIsPinned());
        }
        if (blogCreateDTO.getAllowComment() != null) {
            blog.setAllowComment(blogCreateDTO.getAllowComment());
        }

        Blog savedBlog = blogRepository.save(blog);
        return blogMapper.toBlogResponseDTO(savedBlog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getAllBlogs() {
        return blogRepository.findAll()
                .stream()
                .map(blogMapper::toBlogResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getBlogsByAuthorId(UUID authorUserId) {
        return blogRepository.findByAuthorUserId(authorUserId)
                .stream()
                .map(blogMapper::toBlogResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BlogResponseDTO getBlogById(UUID blogId) {
        return blogMapper.toBlogResponseDTO(findBlogById(blogId));
    }

    @Override
    @Transactional
    public BlogResponseDTO updateBlog(UUID blogId, BlogUpdateDTO blogUpdateDTO) {
        Blog blog = findBlogById(blogId);

        if (blogUpdateDTO.getPageId() != null) {
            blog.setPageId(blogUpdateDTO.getPageId());
        }
        if (blogUpdateDTO.getRegionId() != null) {
            blog.setRegionId(blogUpdateDTO.getRegionId());
        }
        if (blogUpdateDTO.getContent() != null) {
            blog.setContent(blogUpdateDTO.getContent());
        }
        if (blogUpdateDTO.getImageUrls() != null) {
            blog.setImageUrls(blogUpdateDTO.getImageUrls());
        }
        if (blogUpdateDTO.getStatus() != null) {
            blog.setStatus(blogUpdateDTO.getStatus());
        }
        if (blogUpdateDTO.getIsPinned() != null) {
            blog.setIsPinned(blogUpdateDTO.getIsPinned());
        }
        if (blogUpdateDTO.getAllowComment() != null) {
            blog.setAllowComment(blogUpdateDTO.getAllowComment());
        }

        Blog updatedBlog = blogRepository.save(blog);
        return blogMapper.toBlogResponseDTO(updatedBlog);
    }

    @Override
    @Transactional
    public void deleteBlog(UUID blogId) {
        Blog blog = findBlogById(blogId);
        blogRepository.delete(blog);
    }

    private Blog findBlogById(UUID blogId) {
        return blogRepository.findById(blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author user not found"));
    }
}
