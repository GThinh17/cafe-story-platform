package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.BlogShareResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogShare;
import com.cafestory.entity.enums.ActorContextType;
import com.cafestory.entity.enums.ShareType;
import com.cafestory.mapper.BlogInteractionMapper;
import com.cafestory.repository.BlogShareRepository;
import com.cafestory.service.model.ActorContext;
import com.cafestory.service.serviceInterface.BlogShareService;
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
public class BlogShareServiceImpl implements BlogShareService {

    private final BlogShareRepository blogShareRepository;
    private final BlogInteractionMapper blogInteractionMapper;
    private final BlogValidator blogValidator;
    private final ActorContextResolver actorContextResolver;
    private final UserValidator userValidator;

    public BlogShareServiceImpl(
            BlogShareRepository blogShareRepository,
            BlogInteractionMapper blogInteractionMapper,
            BlogValidator blogValidator,
            ActorContextResolver actorContextResolver,
            UserValidator userValidator) {
        this.blogShareRepository = blogShareRepository;
        this.blogInteractionMapper = blogInteractionMapper;
        this.blogValidator = blogValidator;
        this.actorContextResolver = actorContextResolver;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0"),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE, allEntries = true)
    })
    public BlogShareResponseDTO shareBlog(UUID blogId, UUID userId, ShareType shareType) {
        return shareBlog(blogId, userId, shareType, ActorContextType.USER, null);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0"),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE, allEntries = true)
    })
    public BlogShareResponseDTO shareBlog(
            UUID blogId,
            UUID userId,
            ShareType shareType,
            ActorContextType actorContextType,
            UUID actorCafePageId) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        ActorContext actorContext = actorContextResolver.resolve(userId, actorContextType, actorCafePageId);
        ShareType resolvedShareType = resolveShareType(shareType);
        validateShareTypeAllowed(blog, resolvedShareType);

        BlogShare blogShare = new BlogShare();
        blogShare.setBlog(blog);
        blogShare.setUser(actorContext.actorUser());
        blogShare.setActorContextType(actorContext.actorContextType());
        blogShare.setActorCafePage(actorContext.actorCafePage());
        blogShare.setShareType(resolvedShareType);

        BlogShare savedBlogShare = blogShareRepository.save(blogShare);
        incrementShareCount(blog);
        return blogInteractionMapper.toBlogShareResponseDTO(savedBlogShare);
    }

    /**
     * Mirror of {@link #shareBlog}: the web client treats sharing as a toggle on
     * {@code isShared}, so unsharing clears every share this user made on the blog
     * and gives the counter back the same number of points sharing took.
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0"),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE, allEntries = true)
    })
    public void deleteShare(UUID blogId, UUID userId) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        userValidator.validateUserExists(userId);

        List<BlogShare> shares = blogShareRepository.findByUserUserIdAndBlogId(userId, blogId);
        if (shares.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog share not found");
        }

        blogShareRepository.deleteAll(shares);
        decrementShareCount(blog, shares.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogShareResponseDTO> getSharesByBlogId(UUID blogId) {
        blogValidator.validateBlogExists(blogId);
        return blogShareRepository.findByBlogId(blogId)
                .stream()
                .map(blogInteractionMapper::toBlogShareResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogShareResponseDTO> getSharesByUserId(UUID userId) {
        userValidator.validateUserExists(userId);
        return blogShareRepository.findByUserUserId(userId)
                .stream()
                .map(blogInteractionMapper::toBlogShareResponseDTO)
                .toList();
    }

    private void incrementShareCount(Blog blog) {
        int currentCount = blog.getShareCount() == null ? 0 : blog.getShareCount();
        blog.setShareCount(currentCount + 1);
    }

    private void decrementShareCount(Blog blog, int amount) {
        int currentCount = blog.getShareCount() == null ? 0 : blog.getShareCount();
        blog.setShareCount(Math.max(0, currentCount - amount));
    }

    private ShareType resolveShareType(ShareType shareType) {
        return shareType == null ? ShareType.PUBLIC : shareType;
    }

    private void validateShareTypeAllowed(Blog blog, ShareType shareType) {
        if (shareType == ShareType.PAGE_ONLY && blog.getPageId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page only share requires a blog that belongs to a page");
        }
    }
}
