package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.responseDTO.BlogRatingResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogRating;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.mapper.BlogInteractionMapper;
import com.cafestory.repository.BlogRatingRepository;
import com.cafestory.service.serviceInterface.BlogRatingService;
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
public class BlogRatingServiceImpl implements BlogRatingService {

    private final BlogRatingRepository blogRatingRepository;
    private final BlogInteractionMapper blogInteractionMapper;
    private final BlogValidator blogValidator;
    private final UserValidator userValidator;

    public BlogRatingServiceImpl(
            BlogRatingRepository blogRatingRepository,
            BlogInteractionMapper blogInteractionMapper,
            BlogValidator blogValidator,
            UserValidator userValidator) {
        this.blogRatingRepository = blogRatingRepository;
        this.blogInteractionMapper = blogInteractionMapper;
        this.blogValidator = blogValidator;
        this.userValidator = userValidator;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ORGANIC_FEED_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0"),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE, allEntries = true)
    })
    public BlogRatingResponseDTO rateBlog(UUID blogId, UUID userId, Integer rating) {
        validateRating(rating);
        Blog blog = validatePublishedBlog(blogId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        BlogRating blogRating = blogRatingRepository.findByUserUserIdAndBlogId(userId, blogId)
                .orElseGet(() -> createRating(blog, user));
        blogRating.setRating(rating);

        BlogRating savedRating = blogRatingRepository.save(blogRating);
        return enrich(blogInteractionMapper.toBlogRatingResponseDTO(savedRating), blogId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ORGANIC_FEED_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.BLOG_DETAIL_CACHE, key = "#p0"),
            @CacheEvict(cacheNames = CacheConfig.USER_PROFILE_BLOGS_CACHE, allEntries = true)
    })
    public void deleteRating(UUID blogId, UUID userId) {
        blogValidator.validateBlogExists(blogId);
        User user = userValidator.validateUserExists(userId);
        userValidator.validateUserActive(user);

        BlogRating blogRating = blogRatingRepository.findByUserUserIdAndBlogId(userId, blogId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog rating not found"));

        blogRatingRepository.delete(blogRating);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogRatingResponseDTO> getRatingsByBlogId(UUID blogId) {
        blogValidator.validateBlogExists(blogId);
        long ratingCount = blogRatingRepository.countByBlogId(blogId);
        double ratingAverage = averageRating(blogId);
        return blogRatingRepository.findByBlogId(blogId)
                .stream()
                .map(blogInteractionMapper::toBlogRatingResponseDTO)
                .map(response -> enrich(response, ratingAverage, ratingCount))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogRatingResponseDTO> getRatingsByUserId(UUID userId) {
        userValidator.validateUserExists(userId);
        return blogRatingRepository.findByUserUserId(userId)
                .stream()
                .map(blogInteractionMapper::toBlogRatingResponseDTO)
                .map(response -> enrich(response, response.getBlogId()))
                .toList();
    }

    private BlogRating createRating(Blog blog, User user) {
        BlogRating blogRating = new BlogRating();
        blogRating.setBlog(blog);
        blogRating.setUser(user);
        return blogRating;
    }

    private Blog validatePublishedBlog(UUID blogId) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        if (blog.getStatus() != PostStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Blog is not available for interaction");
        }
        return blog;
    }

    private void validateRating(Integer rating) {
        if (rating == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating is required");
        }
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
    }

    private BlogRatingResponseDTO enrich(BlogRatingResponseDTO response, UUID blogId) {
        return enrich(response, averageRating(blogId), blogRatingRepository.countByBlogId(blogId));
    }

    private BlogRatingResponseDTO enrich(BlogRatingResponseDTO response, Double ratingAverage, Long ratingCount) {
        response.setRatingAverage(ratingAverage);
        response.setRatingCount(ratingCount);
        return response;
    }

    private double averageRating(UUID blogId) {
        Double average = blogRatingRepository.findAverageRatingByBlogId(blogId);
        return average == null ? 0.0 : average;
    }
}
