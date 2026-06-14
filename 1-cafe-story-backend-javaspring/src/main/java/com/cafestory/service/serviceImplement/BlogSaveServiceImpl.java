package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.BlogSaveResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogEvent;
import com.cafestory.entity.BlogSave;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.BlogEventType;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.mapper.BlogInteractionMapper;
import com.cafestory.repository.BlogEventRepository;
import com.cafestory.repository.BlogSaveRepository;
import com.cafestory.service.serviceInterface.BlogSaveService;
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
public class BlogSaveServiceImpl implements BlogSaveService {

    private final BlogSaveRepository blogSaveRepository;
    private final BlogEventRepository blogEventRepository;
    private final BlogInteractionMapper blogInteractionMapper;
    private final BlogValidator blogValidator;
    private final UserValidator userValidator;

    public BlogSaveServiceImpl(
            BlogSaveRepository blogSaveRepository,
            BlogEventRepository blogEventRepository,
            BlogInteractionMapper blogInteractionMapper,
            BlogValidator blogValidator,
            UserValidator userValidator) {
        this.blogSaveRepository = blogSaveRepository;
        this.blogEventRepository = blogEventRepository;
        this.blogInteractionMapper = blogInteractionMapper;
        this.blogValidator = blogValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ORGANIC_FEED_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0")
    })
    public BlogSaveResponseDTO saveBlog(UUID blogId, UUID userId) {
        Blog blog = validatePublishedBlog(blogId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        BlogSave blogSave = blogSaveRepository.findByUserUserIdAndBlogId(userId, blogId)
                .orElseGet(() -> createSave(blog, user));

        return enrich(blogInteractionMapper.toBlogSaveResponseDTO(blogSave), blogId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ORGANIC_FEED_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0")
    })
    public void unsaveBlog(UUID blogId, UUID userId) {
        blogValidator.validateBlogExists(blogId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        BlogSave blogSave = blogSaveRepository.findByUserUserIdAndBlogId(userId, blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog save not found"));

        blogSaveRepository.delete(blogSave);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogSaveResponseDTO> getSavesByBlogId(UUID blogId) {
        blogValidator.validateBlogExists(blogId);
        long saveCount = blogSaveRepository.countByBlogId(blogId);
        return blogSaveRepository.findByBlogId(blogId)
                .stream()
                .map(blogInteractionMapper::toBlogSaveResponseDTO)
                .map(response -> enrich(response, true, saveCount))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogSaveResponseDTO> getSavesByUserId(UUID userId) {
        userValidator.validateUserExists(userId);
        return blogSaveRepository.findByUserUserId(userId)
                .stream()
                .map(blogInteractionMapper::toBlogSaveResponseDTO)
                .map(response -> enrich(response, response.getBlogId()))
                .toList();
    }

    private BlogSave createSave(Blog blog, User user) {
        BlogSave blogSave = new BlogSave();
        blogSave.setBlog(blog);
        blogSave.setUser(user);

        BlogSave savedBlogSave = blogSaveRepository.save(blogSave);
        recordSaveEvent(blog, user);
        return savedBlogSave;
    }

    private void recordSaveEvent(Blog blog, User user) {
        BlogEvent blogEvent = new BlogEvent();
        blogEvent.setBlog(blog);
        blogEvent.setUser(user);
        blogEvent.setEventType(BlogEventType.SAVE);
        blogEvent.setWeight(5.0);
        blogEventRepository.save(blogEvent);
    }

    private Blog validatePublishedBlog(UUID blogId) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        if (blog.getStatus() != PostStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Blog is not available for interaction");
        }
        return blog;
    }

    private BlogSaveResponseDTO enrich(BlogSaveResponseDTO response, UUID blogId) {
        return enrich(response, true, blogSaveRepository.countByBlogId(blogId));
    }

    private BlogSaveResponseDTO enrich(BlogSaveResponseDTO response, Boolean saved, Long saveCount) {
        response.setSaved(saved);
        response.setSaveCount(saveCount);
        return response;
    }
}
