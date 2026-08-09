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
import com.cafestory.service.serviceInterface.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiBlogModerationServiceImplTest {

    private AiModerationResultRepository moderationResultRepository;
    private BlogRepository blogRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        moderationResultRepository = mock(AiModerationResultRepository.class);
        blogRepository = mock(BlogRepository.class);
        notificationService = mock(NotificationService.class);
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

    @Test
    void moderateBlog_success_deniedBlogIsRemovedAndAuthorNotified_TC003() {
        Blog blog = blog();
        AiBlogModerationResponseDTO aiResponse = aiResponse("deny", 95, 90);
        AiBlogModerationServiceImpl service = serviceReturning(aiResponse);
        when(blogRepository.save(blog)).thenReturn(blog);

        Blog result = service.moderateBlog(blog);

        assertThat(result.getStatus()).isEqualTo(PostStatus.REMOVED);
        verify(notificationService).createModerationNotification(
                org.mockito.ArgumentMatchers.eq(blog.getAuthor().getUserId()),
                org.mockito.ArgumentMatchers.eq(blog.getId()),
                org.mockito.ArgumentMatchers.eq("DENIED"),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void moderateBlog_success_notificationFailureDoesNotBreakModeration_TC004() {
        Blog blog = blog();
        AiBlogModerationServiceImpl service = serviceReturning(aiResponse("approve", 5, 5));
        when(blogRepository.save(blog)).thenReturn(blog);
        org.mockito.Mockito.doThrow(new IllegalStateException("notification down"))
                .when(notificationService)
                .createModerationNotification(any(UUID.class), any(UUID.class), anyString(), any());

        assertThat(service.moderateBlog(blog).getStatus()).isEqualTo(PostStatus.PUBLISHED);
        verify(moderationResultRepository).save(any(AiModerationResult.class));
    }

    // ------------------------------------------- Gọi thật dịch vụ AI Python

    @Test
    void callAiService_success_parsesResponseAndCapsImageUrls_TC005() throws Exception {
        Blog blog = blog();
        blog.setImageUrls(java.util.stream.IntStream.range(0, 15)
                .mapToObj(index -> "https://cdn.example.com/" + index + ".png")
                .toList());
        when(blogRepository.save(blog)).thenReturn(blog);

        withAiService(200, """
                {"blogId":"%s","captionScore":10,"imageScore":20,"tags":["cafe"],"status":"approve"}
                """.formatted(blog.getId()), service -> {
            assertThat(service.moderateBlog(blog).getStatus()).isEqualTo(PostStatus.PUBLISHED);
        });

        // Chỉ 10 ảnh đầu được gửi sang dịch vụ AI.
        assertThat(requestBody).hasSize(1);
        assertThat(requestBody.get(0)).contains("https://cdn.example.com/9.png");
        assertThat(requestBody.get(0)).doesNotContain("https://cdn.example.com/10.png");
    }

    @Test
    void callAiService_fail_emptyBodyFallsBackToAdminReview_TC006() throws Exception {
        Blog blog = blog();
        when(blogRepository.save(blog)).thenReturn(blog);

        withAiService(200, "   ", service -> service.moderateBlog(blog));

        ArgumentCaptor<AiModerationResult> captor = ArgumentCaptor.forClass(AiModerationResult.class);
        verify(moderationResultRepository).save(captor.capture());
        assertThat(captor.getValue().getAiStatus()).isEqualTo("SEND_ADMIN");
        assertThat(captor.getValue().getImageReason()).contains("AI moderation response is empty");
    }

    @Test
    void callAiService_fail_invalidJsonFallsBackToAdminReview_TC007() throws Exception {
        Blog blog = blog();
        when(blogRepository.save(blog)).thenReturn(blog);

        withAiService(200, "<html>khong phai json</html>", service -> service.moderateBlog(blog));

        ArgumentCaptor<AiModerationResult> captor = ArgumentCaptor.forClass(AiModerationResult.class);
        verify(moderationResultRepository).save(captor.capture());
        assertThat(captor.getValue().getImageReason()).contains("not valid JSON");
    }

    @Test
    void callAiService_success_blogWithoutImagesSendsEmptyList_TC008() throws Exception {
        Blog blog = blog();
        blog.setImageUrls(null);
        when(blogRepository.save(blog)).thenReturn(blog);

        withAiService(200, """
                {"blogId":"%s","captionScore":0,"imageScore":0,"status":"send Admin"}
                """.formatted(blog.getId()), service -> {
            assertThat(service.moderateBlog(blog).getStatus()).isEqualTo(PostStatus.HIDDEN);
        });

        ArgumentCaptor<AiModerationResult> captor = ArgumentCaptor.forClass(AiModerationResult.class);
        verify(moderationResultRepository).save(captor.capture());
        assertThat(captor.getValue().getTags()).isEmpty();
        assertThat(captor.getValue().getModelName()).isEqualTo("cafestory-python-ai");
    }

    private final java.util.List<String> requestBody = new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * Dựng dịch vụ AI Python giả trong tiến trình để chạy đúng
     * {@code callAiService} thật thay vì ghi đè phương thức.
     */
    private void withAiService(
            int statusCode, String body, java.util.function.Consumer<AiBlogModerationServiceImpl> action)
            throws Exception {
        com.sun.net.httpserver.HttpServer server =
                com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/ai/blogs/evaluate", exchange -> {
            requestBody.add(new String(
                    exchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            byte[] payload = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, payload.length);
            try (java.io.OutputStream out = exchange.getResponseBody()) {
                out.write(payload);
            }
        });
        server.start();
        try {
            action.accept(new AiBlogModerationServiceImpl(
                    moderationResultRepository,
                    blogRepository,
                    notificationService,
                    new ObjectMapper(),
                    "http://127.0.0.1:" + server.getAddress().getPort(),
                    5_000));
        } finally {
            server.stop(0);
        }
    }

    private AiBlogModerationServiceImpl serviceReturning(AiBlogModerationResponseDTO response) {
        return new AiBlogModerationServiceImpl(
                moderationResultRepository,
                blogRepository,
                notificationService,
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
                notificationService,
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
