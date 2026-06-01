package com.cafestory.service;

import com.cafestory.dto.responseDTO.BlogRatingResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogRating;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.mapper.BlogInteractionMapper;
import com.cafestory.repository.BlogRatingRepository;
import com.cafestory.service.serviceImplement.BlogRatingServiceImpl;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogRatingServiceImplTest {

    @Mock
    private BlogRatingRepository blogRatingRepository;

    @Mock
    private BlogInteractionMapper blogInteractionMapper;

    @Mock
    private BlogValidator blogValidator;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private BlogRatingServiceImpl blogRatingService;

    @Test
    void rateBlog_success_createsRating_TC001() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Blog blog = blog(blogId, PostStatus.PUBLISHED);
        User user = user(userId);
        BlogRating savedRating = rating(UUID.randomUUID(), blog, user, 4);
        BlogRatingResponseDTO response = response(savedRating.getId(), blogId, userId, 4);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(blogRatingRepository.findByUserUserIdAndBlogId(userId, blogId)).thenReturn(Optional.empty());
        when(blogRatingRepository.save(any(BlogRating.class))).thenReturn(savedRating);
        when(blogInteractionMapper.toBlogRatingResponseDTO(savedRating)).thenReturn(response);
        when(blogRatingRepository.findAverageRatingByBlogId(blogId)).thenReturn(4.5);
        when(blogRatingRepository.countByBlogId(blogId)).thenReturn(2L);

        BlogRatingResponseDTO result = blogRatingService.rateBlog(blogId, userId, 4);

        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getRatingAverage()).isEqualTo(4.5);
        assertThat(result.getRatingCount()).isEqualTo(2L);
    }

    @Test
    void rateBlog_success_updatesExistingRating_TC002() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Blog blog = blog(blogId, PostStatus.PUBLISHED);
        User user = user(userId);
        BlogRating existingRating = rating(UUID.randomUUID(), blog, user, 2);
        BlogRatingResponseDTO response = response(existingRating.getId(), blogId, userId, 5);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(blogRatingRepository.findByUserUserIdAndBlogId(userId, blogId)).thenReturn(Optional.of(existingRating));
        when(blogRatingRepository.save(existingRating)).thenReturn(existingRating);
        when(blogInteractionMapper.toBlogRatingResponseDTO(existingRating)).thenReturn(response);
        when(blogRatingRepository.findAverageRatingByBlogId(blogId)).thenReturn(5.0);
        when(blogRatingRepository.countByBlogId(blogId)).thenReturn(1L);

        BlogRatingResponseDTO result = blogRatingService.rateBlog(blogId, userId, 5);

        assertThat(existingRating.getRating()).isEqualTo(5);
        assertThat(result.getRatingAverage()).isEqualTo(5.0);
    }

    @Test
    void rateBlog_fail_ratingOutOfRange_TC003() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> blogRatingService.rateBlog(blogId, userId, 6))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Rating must be between 1 and 5"));

        verify(blogValidator, never()).validateBlogExists(blogId);
        verify(blogRatingRepository, never()).save(any(BlogRating.class));
    }

    @Test
    void deleteRating_fail_ratingNotFound_TC004() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(userValidator.validateUserExists(userId)).thenReturn(user(userId));
        when(blogRatingRepository.findByUserUserIdAndBlogId(userId, blogId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogRatingService.deleteRating(blogId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Blog rating not found"));
    }

    private Blog blog(UUID blogId, PostStatus status) {
        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setAuthor(user(UUID.randomUUID()));
        blog.setContent("Blog content");
        blog.setStatus(status);
        return blog;
    }

    private User user(UUID userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName("tester");
        user.setUserPassword("123456");
        user.setUserEmail("tester@example.com");
        user.setAccountStatus(true);
        return user;
    }

    private BlogRating rating(UUID id, Blog blog, User user, Integer ratingValue) {
        BlogRating blogRating = new BlogRating();
        blogRating.setId(id);
        blogRating.setBlog(blog);
        blogRating.setUser(user);
        blogRating.setRating(ratingValue);
        return blogRating;
    }

    private BlogRatingResponseDTO response(UUID id, UUID blogId, UUID userId, Integer ratingValue) {
        BlogRatingResponseDTO response = new BlogRatingResponseDTO();
        response.setId(id);
        response.setBlogId(blogId);
        response.setUserId(userId);
        response.setRating(ratingValue);
        return response;
    }
}
