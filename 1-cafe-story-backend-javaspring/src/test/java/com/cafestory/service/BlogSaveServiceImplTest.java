package com.cafestory.service;

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
import com.cafestory.service.serviceImplement.BlogSaveServiceImpl;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class BlogSaveServiceImplTest {

    @Mock
    private BlogSaveRepository blogSaveRepository;

    @Mock
    private BlogEventRepository blogEventRepository;

    @Mock
    private BlogInteractionMapper blogInteractionMapper;

    @Mock
    private BlogValidator blogValidator;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private BlogSaveServiceImpl blogSaveService;

    @Test
    void saveBlog_success_createsSaveAndSaveEvent_TC001() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Blog blog = blog(blogId, PostStatus.PUBLISHED);
        User user = user(userId);
        BlogSave savedBlogSave = blogSave(UUID.randomUUID(), blog, user);
        BlogSaveResponseDTO response = response(savedBlogSave.getId(), blogId, userId);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(blogSaveRepository.findByUserUserIdAndBlogId(userId, blogId)).thenReturn(Optional.empty());
        when(blogSaveRepository.save(any(BlogSave.class))).thenReturn(savedBlogSave);
        when(blogInteractionMapper.toBlogSaveResponseDTO(savedBlogSave)).thenReturn(response);
        when(blogSaveRepository.countByBlogId(blogId)).thenReturn(1L);

        BlogSaveResponseDTO result = blogSaveService.saveBlog(blogId, userId);

        assertThat(result.getSaved()).isTrue();
        assertThat(result.getSaveCount()).isEqualTo(1L);

        ArgumentCaptor<BlogEvent> eventCaptor = ArgumentCaptor.forClass(BlogEvent.class);
        verify(blogEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo(BlogEventType.SAVE);
        assertThat(eventCaptor.getValue().getWeight()).isEqualTo(5.0);
    }

    @Test
    void saveBlog_success_existingSaveIsIdempotent_TC002() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Blog blog = blog(blogId, PostStatus.PUBLISHED);
        User user = user(userId);
        BlogSave existingSave = blogSave(UUID.randomUUID(), blog, user);
        BlogSaveResponseDTO response = response(existingSave.getId(), blogId, userId);

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(blogSaveRepository.findByUserUserIdAndBlogId(userId, blogId)).thenReturn(Optional.of(existingSave));
        when(blogInteractionMapper.toBlogSaveResponseDTO(existingSave)).thenReturn(response);
        when(blogSaveRepository.countByBlogId(blogId)).thenReturn(1L);

        BlogSaveResponseDTO result = blogSaveService.saveBlog(blogId, userId);

        assertThat(result.getSaved()).isTrue();
        assertThat(result.getSaveCount()).isEqualTo(1L);
        verify(blogSaveRepository, never()).save(any(BlogSave.class));
        verify(blogEventRepository, never()).save(any(BlogEvent.class));
    }

    @Test
    void saveBlog_fail_blogNotPublished_TC003() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog(blogId, PostStatus.HIDDEN));

        assertThatThrownBy(() -> blogSaveService.saveBlog(blogId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(blogSaveRepository, never()).save(any(BlogSave.class));
    }

    @Test
    void unsaveBlog_success_TC004() {
        UUID blogId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        BlogSave blogSave = blogSave(UUID.randomUUID(), blog(blogId, PostStatus.PUBLISHED), user);

        when(userValidator.validateUserExists(userId)).thenReturn(user);
        when(blogSaveRepository.findByUserUserIdAndBlogId(userId, blogId)).thenReturn(Optional.of(blogSave));

        blogSaveService.unsaveBlog(blogId, userId);

        verify(blogValidator).validateBlogExists(blogId);
        verify(blogSaveRepository).delete(blogSave);
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

    private BlogSave blogSave(UUID id, Blog blog, User user) {
        BlogSave blogSave = new BlogSave();
        blogSave.setId(id);
        blogSave.setBlog(blog);
        blogSave.setUser(user);
        return blogSave;
    }

    private BlogSaveResponseDTO response(UUID id, UUID blogId, UUID userId) {
        BlogSaveResponseDTO response = new BlogSaveResponseDTO();
        response.setId(id);
        response.setBlogId(blogId);
        response.setUserId(userId);
        return response;
    }
}
