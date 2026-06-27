package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.BlogLikeResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogLike;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.mapper.BlogInteractionMapper;
import com.cafestory.repository.BlogLikeRepository;
import com.cafestory.service.model.ActorContext;
import com.cafestory.service.serviceInterface.BlogLikeService;
import com.cafestory.validation.ActorContextResolver;
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
    private final ActorContextResolver actorContextResolver;
    private final UserValidator userValidator;

    public BlogLikeServiceImpl(
            BlogLikeRepository blogLikeRepository,
            BlogInteractionMapper blogInteractionMapper,
            BlogValidator blogValidator,
            ActorContextResolver actorContextResolver,
            UserValidator userValidator) {
        this.blogLikeRepository = blogLikeRepository;
        this.blogInteractionMapper = blogInteractionMapper;
        this.blogValidator = blogValidator;
        this.actorContextResolver = actorContextResolver;
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
        return likeBlog(blogId, userId, ActorContextType.USER, null);
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
    public BlogLikeResponseDTO likeBlog(UUID blogId, UUID userId, ActorContextType actorContextType, UUID actorCafePageId) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        ActorContext actorContext = actorContextResolver.resolve(userId, actorContextType, actorCafePageId);
        UUID resolvedActorCafePageId = actorContext.actorCafePage() == null ? null : actorContext.actorCafePage().getId();
        if (blogLikeRepository.existsByActor(userId, blogId, actorContext.actorContextType(), resolvedActorCafePageId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Blog already liked by actor");
        }

        BlogLike blogLike = new BlogLike();
        blogLike.setBlog(blog);
        blogLike.setUser(actorContext.actorUser());
        blogLike.setActorContextType(actorContext.actorContextType());
        blogLike.setActorCafePage(actorContext.actorCafePage());

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
        unlikeBlog(blogId, userId, ActorContextType.USER, null);
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
    public void unlikeBlog(UUID blogId, UUID userId, ActorContextType actorContextType, UUID actorCafePageId) {
        blogValidator.validateBlogExists(blogId);
        ActorContext actorContext = actorContextResolver.resolve(userId, actorContextType, actorCafePageId);
        UUID resolvedActorCafePageId = actorContext.actorCafePage() == null ? null : actorContext.actorCafePage().getId();

        BlogLike blogLike = blogLikeRepository.findByActor(userId, blogId, actorContext.actorContextType(), resolvedActorCafePageId)
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
