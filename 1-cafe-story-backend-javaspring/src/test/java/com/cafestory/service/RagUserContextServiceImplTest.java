package com.cafestory.service;

import com.cafestory.dto.responseDTO.RagBlogModerationItemResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.service.serviceImplement.RagUserContextServiceImpl;
import com.cafestory.until.security.JwtClaims;
import com.cafestory.until.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagUserContextServiceImplTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private AiModerationResultRepository aiModerationResultRepository;

    private RagUserContextServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RagUserContextServiceImpl(jwtService, aiModerationResultRepository);
    }

    @Test
    void invalidJwtThrowsForbidden() {
        when(jwtService.validateAccessToken("bad-token")).thenThrow(new RuntimeException("expired"));

        assertThatThrownBy(() -> service.getBlogModerationForUser("bad-token", 5))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid user token");
    }

    @Test
    void returnsOnlySafeFieldsForOwnUser() {
        UUID userId = UUID.randomUUID();
        when(jwtService.validateAccessToken("good-token"))
                .thenReturn(new JwtClaims(userId, "vu", List.of("USER"), Instant.now(), Instant.now()));

        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setStatus(PostStatus.HIDDEN);
        AiModerationResult result = new AiModerationResult();
        result.setBlog(blog);
        result.setDecision(ModerationDecision.VIOLATION);
        result.setCaption("Noi dung bi tu choi vi khong lien quan cafe");
        result.setCaptionReason("Khong lien quan den cafe");
        result.setImageReason("Anh khong phai quan cafe");
        result.setResolved(false);
        result.setCreatedAt(LocalDateTime.now());
        when(aiModerationResultRepository.findRagUserModerationResults(eq(userId), any(Pageable.class)))
                .thenReturn(List.of(result));

        List<RagBlogModerationItemResponseDTO> items =
                service.getBlogModerationForUser("good-token", 5);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getDecision()).isEqualTo("VIOLATION");
        assertThat(items.get(0).getCaptionReason()).isEqualTo("Khong lien quan den cafe");
        assertThat(items.get(0).getBlogStatus()).isEqualTo("HIDDEN");
    }

    @Test
    void captionIsTruncatedToSnippet() {
        UUID userId = UUID.randomUUID();
        when(jwtService.validateAccessToken("good-token"))
                .thenReturn(new JwtClaims(userId, "vu", List.of("USER"), Instant.now(), Instant.now()));

        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        AiModerationResult result = new AiModerationResult();
        result.setBlog(blog);
        result.setCaption("x".repeat(500));
        result.setCreatedAt(LocalDateTime.now());
        when(aiModerationResultRepository.findRagUserModerationResults(eq(userId), any(Pageable.class)))
                .thenReturn(List.of(result));

        List<RagBlogModerationItemResponseDTO> items =
                service.getBlogModerationForUser("good-token", 5);

        assertThat(items.get(0).getCaptionSnippet().length()).isLessThanOrEqualTo(121);
    }
}
