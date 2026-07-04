package com.cafestory.service;

import com.cafestory.dto.responseDTO.ReportModerationResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Comment;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.ReportReason;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.service.serviceImplement.ReportModerationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ReportModerationServiceImplTest {

    private AiModerationResultRepository moderationResultRepository;

    @BeforeEach
    void setUp() {
        moderationResultRepository = mock(AiModerationResultRepository.class);
    }

    @Test
    void moderateReport_success_blogReportPersistsAiResult_TC001() {
        ContentReport report = blogReport();
        ReportModerationResponseDTO response = response(ModerationDecision.VIOLATION, 91.4);
        ReportModerationServiceImpl service = serviceReturning(response);

        service.moderateReport(report);

        ArgumentCaptor<AiModerationResult> resultCaptor = ArgumentCaptor.forClass(AiModerationResult.class);
        verify(moderationResultRepository).save(resultCaptor.capture());
        AiModerationResult savedResult = resultCaptor.getValue();
        assertThat(savedResult.getBlog()).isEqualTo(report.getBlog());
        assertThat(savedResult.getComment()).isNull();
        assertThat(savedResult.getCaption()).isEqualTo("Reported blog content");
        assertThat(savedResult.getDecision()).isEqualTo(ModerationDecision.VIOLATION);
        assertThat(savedResult.getScore()).isEqualTo(91.4);
        assertThat(savedResult.getCaptionScore()).isEqualTo(91);
        assertThat(savedResult.getTags()).containsExactly("violence", "harassment");
        assertThat(savedResult.getResolved()).isFalse();
        assertThat(savedResult.getRawResponse()).containsKey("rawCategories");
    }

    @Test
    void moderateReport_success_commentReportPersistsAiResult_TC002() {
        ContentReport report = commentReport();
        ReportModerationResponseDTO response = response(ModerationDecision.NEEDS_REVIEW, 60.0);
        ReportModerationServiceImpl service = serviceReturning(response);

        service.moderateReport(report);

        ArgumentCaptor<AiModerationResult> resultCaptor = ArgumentCaptor.forClass(AiModerationResult.class);
        verify(moderationResultRepository).save(resultCaptor.capture());
        AiModerationResult savedResult = resultCaptor.getValue();
        assertThat(savedResult.getBlog()).isEqualTo(report.getComment().getBlog());
        assertThat(savedResult.getComment()).isEqualTo(report.getComment());
        assertThat(savedResult.getCaption()).isEqualTo("Reported comment content");
        assertThat(savedResult.getDecision()).isEqualTo(ModerationDecision.NEEDS_REVIEW);
        assertThat(savedResult.getResolved()).isFalse();
    }

    @Test
    void moderateReport_failureWebhookPersistsFallbackNeedsReview_TC003() {
        ContentReport report = blogReport();
        ReportModerationServiceImpl service = serviceThrowing();

        service.moderateReport(report);

        ArgumentCaptor<AiModerationResult> resultCaptor = ArgumentCaptor.forClass(AiModerationResult.class);
        verify(moderationResultRepository).save(resultCaptor.capture());
        AiModerationResult savedResult = resultCaptor.getValue();
        assertThat(savedResult.getDecision()).isEqualTo(ModerationDecision.NEEDS_REVIEW);
        assertThat(savedResult.getExplanation()).contains("Report moderation service unavailable");
        assertThat(savedResult.getModelName()).isEqualTo("cafestory-report-n8n-openai");
        assertThat(savedResult.getResolved()).isFalse();
    }

    @Test
    void moderateReport_unsupportedUserAndCafePageReportsDoNotCallWebhookOrPersist_TC004() {
        ReportModerationServiceImpl service = serviceThrowing();

        service.moderateReport(userReport());
        service.moderateReport(cafePageReport());

        verify(moderationResultRepository, never()).save(org.mockito.ArgumentMatchers.any(AiModerationResult.class));
    }

    private ReportModerationServiceImpl serviceReturning(ReportModerationResponseDTO response) {
        return new ReportModerationServiceImpl(
                moderationResultRepository,
                new ObjectMapper(),
                "http://localhost",
                1000) {
            @Override
            protected ReportModerationResponseDTO callModerationWebhook(
                    com.cafestory.dto.requestDTO.ReportModerationRequestDTO request) {
                return response;
            }
        };
    }

    private ReportModerationServiceImpl serviceThrowing() {
        return new ReportModerationServiceImpl(
                moderationResultRepository,
                new ObjectMapper(),
                "http://localhost",
                1000) {
            @Override
            protected ReportModerationResponseDTO callModerationWebhook(
                    com.cafestory.dto.requestDTO.ReportModerationRequestDTO request) {
                throw new IllegalStateException("timeout");
            }
        };
    }

    private ReportModerationResponseDTO response(ModerationDecision decision, double score) {
        ReportModerationResponseDTO response = new ReportModerationResponseDTO();
        response.setDecision(decision);
        response.setScore(score);
        response.setLabels(List.of("violence", "harassment"));
        response.setExplanation("OpenAI moderation detected policy risk");
        response.setModelName("omni-moderation-latest");
        response.setRawCategories(Map.of("violence", true));
        return response;
    }

    private ContentReport blogReport() {
        ContentReport report = baseReport(ReportTargetType.BLOG);
        report.setBlog(blog());
        return report;
    }

    private ContentReport commentReport() {
        ContentReport report = baseReport(ReportTargetType.COMMENT);
        report.setComment(comment());
        return report;
    }

    private ContentReport userReport() {
        ContentReport report = baseReport(ReportTargetType.USER);
        report.setReportedUser(user("reported"));
        return report;
    }

    private ContentReport cafePageReport() {
        ContentReport report = baseReport(ReportTargetType.CAFE_PAGE);
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        report.setCafePage(cafePage);
        return report;
    }

    private ContentReport baseReport(ReportTargetType targetType) {
        ContentReport report = new ContentReport();
        report.setId(UUID.randomUUID());
        report.setReporter(user("reporter"));
        report.setTargetType(targetType);
        report.setReason(reason());
        report.setReasonSnapshot("Spam or violence");
        report.setDescription("This content looks unsafe");
        return report;
    }

    private Blog blog() {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setAuthor(user("author"));
        blog.setContent("Reported blog content");
        blog.setImageUrls(List.of("https://example.com/blog.png"));
        return blog;
    }

    private Comment comment() {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setBlog(blog());
        comment.setUser(user("commenter"));
        comment.setContent("Reported comment content");
        comment.setImageUrls(List.of("https://example.com/comment.png"));
        return comment;
    }

    private ReportReason reason() {
        ReportReason reason = new ReportReason();
        reason.setId(UUID.randomUUID());
        reason.setCode("VIOLENCE_HATE_OR_EXPLOITATION");
        reason.setLabelVi("Violence");
        return reason;
    }

    private User user(String username) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName(username);
        return user;
    }
}
