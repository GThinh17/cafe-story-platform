package com.cafestory.service;

import com.cafestory.dto.responseDTO.AiBlogModerationResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.service.serviceImplement.AiBlogModerationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiBlogModerationServiceImplTest {

    private AiModerationResultRepository moderationResultRepository;
    private BlogRepository blogRepository;

    @BeforeEach
    void setUp() {
        moderationResultRepository = mock(AiModerationResultRepository.class);
        blogRepository = mock(BlogRepository.class);
    }

    @Test
    void moderateBlog_success_savesAiResultAndPublishesBlog_TC001() {
        Blog blog = blog();
        AiBlogModerationResponseDTO aiResponse = aiResponse("approve", 12, 86);
        AiBlogModerationServiceImpl service = serviceReturning(aiResponse);

        when(blogRepository.save(blog)).thenReturn(blog);

        Blog result = service.moderateBlog(blog);

        assertThat(result.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        ArgumentCaptor<AiModerationResult> resultCaptor = ArgumentCaptor.forClass(AiModerationResult.class);
        verify(moderationResultRepository).save(resultCaptor.capture());
        AiModerationResult savedResult = resultCaptor.getValue();
        assertThat(savedResult.getBlog()).isEqualTo(blog);
        assertThat(savedResult.getCaption()).isEqualTo("Cafe review content");
        assertThat(savedResult.getDecision()).isEqualTo(ModerationDecision.SAFE);
        assertThat(savedResult.getCaptionScore()).isEqualTo(12);
        assertThat(savedResult.getImageScore()).isEqualTo(86);
        assertThat(savedResult.getTags()).containsExactly("study cafe", "brunch cafe", "garden cafe");
        assertThat(savedResult.getAiStatus()).isEqualTo("APPROVE");
        assertThat(savedResult.getResolved()).isTrue();
    }

    @Test
    void moderateBlog_aiFailure_savesNeedsReviewAndHidesBlog_TC002() {
        Blog blog = blog();
        AiBlogModerationServiceImpl service = serviceThrowing();

        when(blogRepository.save(blog)).thenReturn(blog);

        Blog result = service.moderateBlog(blog);

        assertThat(result.getStatus()).isEqualTo(PostStatus.HIDDEN);
        ArgumentCaptor<AiModerationResult> resultCaptor = ArgumentCaptor.forClass(AiModerationResult.class);
        verify(moderationResultRepository).save(resultCaptor.capture());
        AiModerationResult savedResult = resultCaptor.getValue();
        assertThat(savedResult.getCaption()).isEqualTo("Cafe review content");
        assertThat(savedResult.getDecision()).isEqualTo(ModerationDecision.NEEDS_REVIEW);
        assertThat(savedResult.getAiStatus()).isEqualTo("SEND_ADMIN");
        assertThat(savedResult.getCaptionReason()).contains("AI moderation service unavailable");
        assertThat(savedResult.getResolved()).isFalse();
    }

    private AiBlogModerationServiceImpl serviceReturning(AiBlogModerationResponseDTO response) {
        return new AiBlogModerationServiceImpl(
                moderationResultRepository,
                blogRepository,
                new ObjectMapper(),
                "http://localhost:8036",
                1000) {
            @Override
            protected AiBlogModerationResponseDTO callAiService(Blog blog) {
                return response;
            }
        };
    }

    private AiBlogModerationServiceImpl serviceThrowing() {
        return new AiBlogModerationServiceImpl(
                moderationResultRepository,
                blogRepository,
                new ObjectMapper(),
                "http://localhost:8036",
                1000) {
            @Override
            protected AiBlogModerationResponseDTO callAiService(Blog blog) {
                throw new IllegalStateException("timeout");
            }
        };
    }

    private AiBlogModerationResponseDTO aiResponse(String status, int captionScore, int imageScore) {
        AiBlogModerationResponseDTO response = new AiBlogModerationResponseDTO();
        response.setBlogId(UUID.randomUUID());
        response.setCaptionScore(captionScore);
        response.setCaptionReason("Caption ok");
        response.setImageScore(imageScore);
        response.setImageReason("Image ok");
        response.setTags(List.of("study cafe", "brunch cafe", "garden cafe"));
        response.setStatus(status);
        return response;
    }

    private Blog blog() {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setAuthor(user());
        blog.setContent("Cafe review content");
        blog.setImageUrls(List.of("https://example.com/blog-1.png"));
        blog.setStatus(PostStatus.PUBLISHED);
        return blog;
    }

    private User user() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("tester");
        return user;
    }
}
