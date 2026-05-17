package com.cafestory.validation;

import com.cafestory.entity.Blog;
import com.cafestory.repository.BlogRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogValidatorTest {

    @Mock
    private BlogRepository blogRepository;

    @InjectMocks
    private BlogValidator blogValidator;

    @Test
    void validateBlogExists_success_TC001() {
        UUID blogId = UUID.randomUUID();
        Blog blog = new Blog();
        blog.setId(blogId);

        when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));

        Blog result = blogValidator.validateBlogExists(blogId);

        assertThat(result).isEqualTo(blog);
    }

    @Test
    void validateBlogExists_fail_nullBlogId_TC002() {
        assertThatThrownBy(() -> blogValidator.validateBlogExists(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Blog id is required"));

        verify(blogRepository, never()).findById(null);
    }

    @Test
    void validateBlogExists_fail_blogNotFound_TC003() {
        UUID blogId = UUID.randomUUID();

        when(blogRepository.findById(blogId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogValidator.validateBlogExists(blogId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Blog not found"));
    }
}
