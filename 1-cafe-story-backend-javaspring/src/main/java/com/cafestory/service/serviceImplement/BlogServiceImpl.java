package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.BlogCreateDTO;
import com.cafestory.dto.requestDTO.BlogUpdateDTO;
import com.cafestory.dto.responseDTO.BlogResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.User;
import com.cafestory.mapper.BlogMapper;
import com.cafestory.repository.BlogRepository;
import com.cafestory.service.serviceInterface.BlogService;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;
    private final BlogValidator blogValidator;
    private final CafePageValidator cafePageValidator;
    private final UserValidator userValidator;

    public BlogServiceImpl(
            BlogRepository blogRepository,
            BlogMapper blogMapper,
            BlogValidator blogValidator,
            CafePageValidator cafePageValidator,
            UserValidator userValidator) {
        this.blogRepository = blogRepository;
        this.blogMapper = blogMapper;
        this.blogValidator = blogValidator;
        this.cafePageValidator = cafePageValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    public BlogResponseDTO createBlog(BlogCreateDTO blogCreateDTO) {
        User author = userValidator.validateUserExists(blogCreateDTO.getAuthorUserId());
        CafePage page = null;
        if (blogCreateDTO.getPageId() != null) {
            page = cafePageValidator.validateUserCanCreateBlogOnPage(blogCreateDTO.getPageId(), author.getUserId());
        }

        Blog blog = blogMapper.toBlog(blogCreateDTO);
        blog.setAuthor(author);
        blog.setPage(page);
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
        return getAllBlogsByUserId(authorUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponseDTO> getAllBlogsByUserId(UUID userId) {
        userValidator.validateUserExists(userId);
        return blogRepository.findByAuthorUserId(userId)
                .stream()
                .map(blogMapper::toBlogResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BlogResponseDTO getBlogById(UUID blogId) {
        return blogMapper.toBlogResponseDTO(blogValidator.validateBlogExists(blogId));
    }

    @Override
    @Transactional
    public BlogResponseDTO updateBlog(UUID blogId, UUID actorUserId, BlogUpdateDTO blogUpdateDTO) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        validateBlogOwner(blog, actorUserId);

        if (blogUpdateDTO.getPageId() != null) {
            CafePage page = cafePageValidator.validateUserCanCreateBlogOnPage(blogUpdateDTO.getPageId(), actorUserId);
            blog.setPage(page);
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
    public void deleteBlog(UUID blogId, UUID actorUserId) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        validateBlogOwner(blog, actorUserId);
        blogRepository.delete(blog);
    }

    private void validateBlogOwner(Blog blog, UUID actorUserId) {
        if (actorUserId == null || blog.getAuthor() == null || !actorUserId.equals(blog.getAuthor().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not allowed to manage this blog");
        }
    }
}
