package com.cafestory.service;

import com.cafestory.dto.responseDTO.BlogEventResponse;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogEvent;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.BlogEventType;
import com.cafestory.repository.BlogEventRepository;
import com.cafestory.service.serviceImplement.BlogEventServiceImpl;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link BlogEventServiceImpl} — bảng sự kiện tương tác dùng cho xếp hạng.
 *
 * <p>Trọng số mặc định theo loại sự kiện là hằng số nghiệp vụ (REPORT mang dấu âm)
 * nên được kiểm từng giá trị; người dùng ẩn danh vẫn ghi được sự kiện.
 */
@ExtendWith(MockitoExtension.class)
class BlogEventServiceImplTest {

    @Mock
    private BlogEventRepository blogEventRepository;
    @Mock
    private BlogValidator blogValidator;
    @Mock
    private UserValidator userValidator;

    private BlogEventServiceImpl blogEventService;

    private Blog blog;
    private User user;

    @BeforeEach
    void setUp() {
        blogEventService = new BlogEventServiceImpl(blogEventRepository, blogValidator, userValidator);
        blog = blog();
        user = user();
    }

    @ParameterizedTest
    @CsvSource({
            "VIEW, 0.2",
            "LIKE, 2.0",
            "COMMENT, 4.0",
            "SHARE, 6.0",
            "SAVE, 5.0",
            "REPORT, -10.0"
    })
    void recordEvent_success_usesDefaultWeightPerEventType_TC001(BlogEventType eventType, double expectedWeight) {
        givenSavedEvent();
        when(blogValidator.validateBlogExists(blog.getId())).thenReturn(blog);
        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);

        BlogEventResponse result = blogEventService.recordEvent(blog.getId(), user.getUserId(), eventType, null);

        assertThat(result.getEventType()).isEqualTo(eventType);
        assertThat(result.getWeight()).isEqualTo(expectedWeight);
        assertThat(result.getBlogId()).isEqualTo(blog.getId());
        assertThat(result.getUserId()).isEqualTo(user.getUserId());
        verify(userValidator).validateUserActive(user);
    }

    @Test
    void recordEvent_success_explicitWeightWins_TC002() {
        givenSavedEvent();
        when(blogValidator.validateBlogExists(blog.getId())).thenReturn(blog);
        when(userValidator.validateUserExists(user.getUserId())).thenReturn(user);

        BlogEventResponse result =
                blogEventService.recordEvent(blog.getId(), user.getUserId(), BlogEventType.VIEW, 9.5);

        assertThat(result.getWeight()).isEqualTo(9.5);
    }

    @Test
    void recordEvent_success_anonymousEventHasNoUser_TC003() {
        givenSavedEvent();
        when(blogValidator.validateBlogExists(blog.getId())).thenReturn(blog);

        BlogEventResponse result =
                blogEventService.recordEvent(blog.getId(), null, BlogEventType.VIEW, null);

        assertThat(result.getUserId()).isNull();
        assertThat(result.getCreatedAt()).isNotNull();
        verify(userValidator, never()).validateUserExists(any(UUID.class));
        verify(userValidator, never()).validateUserActive(any(User.class));
    }

    private void givenSavedEvent() {
        when(blogEventRepository.save(any(BlogEvent.class))).thenAnswer(invocation -> {
            BlogEvent saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });
    }

    private Blog blog() {
        Blog newBlog = new Blog();
        newBlog.setId(UUID.randomUUID());
        return newBlog;
    }

    private User user() {
        User newUser = new User();
        newUser.setUserId(UUID.randomUUID());
        newUser.setUserName("an");
        newUser.setAccountStatus(true);
        return newUser;
    }
}
