package com.cafestory.controller;

import com.cafestory.dto.responseDTO.BlogSaveResponseDTO;
import com.cafestory.service.serviceInterface.BlogSaveService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link BlogSaveController} — lưu bài viết luôn gắn với người đăng nhập.
 */
@ExtendWith(MockitoExtension.class)
class BlogSaveControllerTest {

    @Mock
    private BlogSaveService blogSaveService;

    @InjectMocks
    private BlogSaveController blogSaveController;

    private AuthenticatedUserPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new AuthenticatedUserPrincipal(UUID.randomUUID(), "an", List.of("USER"));
    }

    @Test
    void saveBlog_success_usesTokenUserId_TC001() {
        UUID blogId = UUID.randomUUID();
        BlogSaveResponseDTO response = new BlogSaveResponseDTO();
        when(blogSaveService.saveBlog(blogId, principal.userId())).thenReturn(response);

        assertThat(blogSaveController.saveBlog(blogId, principal)).isSameAs(response);
    }

    @Test
    void unsaveBlog_success_usesTokenUserId_TC002() {
        UUID blogId = UUID.randomUUID();

        blogSaveController.unsaveBlog(blogId, principal);

        verify(blogSaveService).unsaveBlog(blogId, principal.userId());
    }

    @Test
    void getSavesByBlogId_success_delegates_TC003() {
        UUID blogId = UUID.randomUUID();
        List<BlogSaveResponseDTO> saves = List.of(new BlogSaveResponseDTO());
        when(blogSaveService.getSavesByBlogId(blogId)).thenReturn(saves);

        assertThat(blogSaveController.getSavesByBlogId(blogId)).isSameAs(saves);
    }

    @Test
    void getSavesByUserId_success_delegates_TC004() {
        UUID userId = UUID.randomUUID();
        List<BlogSaveResponseDTO> saves = List.of(new BlogSaveResponseDTO());
        when(blogSaveService.getSavesByUserId(userId)).thenReturn(saves);

        assertThat(blogSaveController.getSavesByUserId(userId)).isSameAs(saves);
    }

    @Test
    void getMySaves_success_usesTokenUserId_TC005() {
        List<BlogSaveResponseDTO> saves = List.of(new BlogSaveResponseDTO());
        when(blogSaveService.getSavesByUserId(principal.userId())).thenReturn(saves);

        assertThat(blogSaveController.getMySaves(principal)).isSameAs(saves);
    }

    @Test
    void getMySaves_fail_missingPrincipal_TC006() {
        assertThatThrownBy(() -> blogSaveController.getMySaves(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication is required");
    }
}
