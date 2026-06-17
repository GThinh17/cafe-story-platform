package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.BlogLikeResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogLike;
import com.cafestory.entity.User;
import com.cafestory.mapper.BlogInteractionMapper;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.service.serviceInterface.BlogLikeService;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.UserValidator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class BlogLikeServiceImpl implements BlogLikeService {

    private final BlogLikeRepository blogLikeRepository;
    private final BlogInteractionMapper blogInteractionMapper;
    private final BlogValidator blogValidator;
    private final UserValidator userValidator;

    public BlogLikeServiceImpl(
            BlogLikeRepository blogLikeRepository,
            BlogInteractionMapper blogInteractionMapper,
            BlogValidator blogValidator,
            UserValidator userValidator) {
        this.blogLikeRepository = blogLikeRepository;
        this.blogInteractionMapper = blogInteractionMapper;
        this.blogValidator = blogValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0"),
            @CacheEvict(cacheNames = {
                    CacheConfig.USER_PROFILE_BY_ID_CACHE,
                    CacheConfig.USER_PROFILE_BY_USERNAME_CACHE
            }, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE, allEntries = true)
    })
    public BlogLikeResponseDTO likeBlog(UUID blogId, UUID userId) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);
        if (blogLikeRepository.existsByUserUserIdAndBlogId(userId, blogId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Blog already liked by user");
        }

        BlogLike blogLike = new BlogLike();
        blogLike.setBlog(blog);
        blogLike.setUser(user);

        BlogLike savedBlogLike = blogLikeRepository.save(blogLike);
        incrementLikeCount(blog);
        incrementAuthorLikeCount(blog);
        return blogInteractionMapper.toBlogLikeResponseDTO(savedBlogLike);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0"),
            @CacheEvict(cacheNames = {
                    CacheConfig.USER_PROFILE_BY_ID_CACHE,
                    CacheConfig.USER_PROFILE_BY_USERNAME_CACHE
            }, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE, allEntries = true)
    })
    public void unlikeBlog(UUID blogId, UUID userId) {
        blogValidator.validateBlogExists(blogId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        BlogLike blogLike = blogLikeRepository.findByUserUserIdAndBlogId(userId, blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog like not found"));

        blogLikeRepository.delete(blogLike);
        decrementLikeCount(blogLike.getBlog());
        decrementAuthorLikeCount(blogLike.getBlog());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogLikeResponseDTO> getLikesByBlogId(UUID blogId) {
        blogValidator.validateBlogExists(blogId);
        return blogLikeRepository.findByBlogId(blogId)
                .stream()
                .map(blogInteractionMapper::toBlogLikeResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogLikeResponseDTO> getLikesByUserId(UUID userId) {
        userValidator.validateUserExists(userId);
        return blogLikeRepository.findByUserUserId(userId)
                .stream()
                .map(blogInteractionMapper::toBlogLikeResponseDTO)
                .toList();
    }

    private void incrementLikeCount(Blog blog) {
        int currentCount = blog.getLikeCount() == null ? 0 : blog.getLikeCount();
        blog.setLikeCount(currentCount + 1);
    }

    private void decrementLikeCount(Blog blog) {
        int currentCount = blog.getLikeCount() == null ? 0 : blog.getLikeCount();
        blog.setLikeCount(Math.max(0, currentCount - 1));
    }

    private void incrementAuthorLikeCount(Blog blog) {
        User author = blog.getAuthor();
        if (author == null) {
            return;
        }
        int currentCount = author.getUserLike() == null ? 0 : author.getUserLike();
        author.setUserLike(currentCount + 1);
    }

    private void decrementAuthorLikeCount(Blog blog) {
        User author = blog.getAuthor();
        if (author == null) {
            return;
        }
        int currentCount = author.getUserLike() == null ? 0 : author.getUserLike();
        author.setUserLike(Math.max(0, currentCount - 1));
    }
}
