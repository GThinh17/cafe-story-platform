package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiResolutionResponseDTO;
import com.cafestory.dto.responseDTO.AdminReportAiResolutionWebhookResponseDTO;
import com.cafestory.entity.AdminReportAiResolution;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Comment;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.ReportReason;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.AdminReportAiResolutionRepository;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.ContentReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminReportAiResolutionServiceImplTest {

    private AdminReportAiResolutionRepository resolutionRepository;
    private ContentReportRepository contentReportRepository;
    private AiModerationResultRepository moderationResultRepository;

    @BeforeEach
    void setUp() {
        resolutionRepository = mock(AdminReportAiResolutionRepository.class);
        contentReportRepository = mock(ContentReportRepository.class);
        moderationResultRepository = mock(AiModerationResultRepository.class);
    }

    @Test
    void createResolution_success_blogReportCallsWebhookAndSavesResolution_TC001() {
        ContentReport report = blogReport();
        CapturingService service = serviceReturning(response(
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                .thenReturn(Optional.empty());
        when(contentReportRepository.countByBlogIdAndStatusIn(any(UUID.class), any())).thenReturn(2L);
        when(resolutionRepository.save(any(AdminReportAiResolution.class)))
                .thenAnswer(invocation -> saved(invocation.getArgument(0)));

        AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

        assertThat(service.lastRequest.getReportId()).isEqualTo(report.getId());
        assertThat(service.lastRequest.getTargetType()).isEqualTo(ReportTargetType.BLOG);
        assertThat(service.lastRequest.getTargetId()).isEqualTo(report.getBlog().getId());
        assertThat(service.lastRequest.getContentText()).isEqualTo("Reported blog content");
        assertThat(service.lastRequest.getImageUrls()).containsExactly("https://example.com/blog.png");
        assertThat(service.lastRequest.getSameTargetOpenReportCount()).isEqualTo(2L);
        assertThat(result.getContentReportId()).isEqualTo(report.getId());
        assertThat(result.getReportDecision()).isEqualTo(AdminReportAiReportDecision.RESOLVE);
        assertThat(result.getTargetAction()).isEqualTo(AdminReportAiTargetAction.HIDE);
        assertThat(result.getModelName()).isEqualTo("gpt-4o-mini");
        verify(resolutionRepository).save(any(AdminReportAiResolution.class));
    }

    @Test
    void createResolution_success_supportedTargets_TC002() {
        for (ContentReport report : List.of(
                commentReport(),
                userReport(),
                cafePageReport())) {
            CapturingService service = serviceReturning(response(
                    AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW,
                    AdminReportAiTargetAction.NONE));
            when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
            when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                    .thenReturn(Optional.empty());
            when(resolutionRepository.save(any(AdminReportAiResolution.class)))
                    .thenAnswer(invocation -> saved(invocation.getArgument(0)));

            AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

            assertThat(result.getContentReportId()).isEqualTo(report.getId());
            assertThat(result.getTargetType()).isEqualTo(report.getTargetType());
            assertThat(service.lastRequest.getTargetId()).isNotNull();
        }
    }

    @Test
    void createResolution_fail_reportNotFound_TC003() {
        UUID reportId = UUID.randomUUID();
        when(contentReportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceReturning(response(
                AdminReportAiReportDecision.REJECT,
                AdminReportAiTargetAction.APPROVE)).createResolution(reportId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Report not found"));

        verify(resolutionRepository, never()).save(any(AdminReportAiResolution.class));
    }

    @Test
    void createResolution_fail_webhookUnavailableDoesNotSave_TC004() {
        ContentReport report = blogReport();
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceThrowing().createResolution(report.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));

        verify(resolutionRepository, never()).save(any(AdminReportAiResolution.class));
    }

    @Test
    void createResolution_fail_malformedWebhookResponseDoesNotSave_TC005() {
        ContentReport report = blogReport();
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceReturning(new AdminReportAiResolutionWebhookResponseDTO())
                .createResolution(report.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));

        verify(resolutionRepository, never()).save(any(AdminReportAiResolution.class));
    }

    @Test
    void createResolution_success_recommendationDoesNotMutateReportOrContent_TC006() {
        ContentReport report = blogReport();
        report.setStatus(ReportStatus.OPEN);
        report.getBlog().setStatus(PostStatus.PUBLISHED);
        ReportStatus reportStatus = report.getStatus();
        PostStatus blogStatus = report.getBlog().getStatus();
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                .thenReturn(Optional.empty());
        when(resolutionRepository.save(any(AdminReportAiResolution.class)))
                .thenAnswer(invocation -> saved(invocation.getArgument(0)));

        serviceReturning(response(AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.REMOVE))
                .createResolution(report.getId());

        assertThat(report.getStatus()).isEqualTo(reportStatus);
        assertThat(report.getResolvedAt()).isNull();
        assertThat(report.getBlog().getStatus()).isEqualTo(blogStatus);
        verify(contentReportRepository, never()).save(any(ContentReport.class));
    }

    @Test
    void getResolutions_success_returnsReportHistory_TC007() {
        ContentReport report = blogReport();
        AdminReportAiResolution resolution = saved(new AdminReportAiResolution());
        resolution.setContentReport(report);
        resolution.setTargetType(report.getTargetType());
        resolution.setTargetId(report.getBlog().getId());
        resolution.setReportDecision(AdminReportAiReportDecision.REJECT);
        resolution.setTargetAction(AdminReportAiTargetAction.APPROVE);
        PageRequest pageable = PageRequest.of(0, 20);
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(resolutionRepository.findByContentReportId(report.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(resolution)));

        Page<AdminReportAiResolutionResponseDTO> result = serviceReturning(response(
                AdminReportAiReportDecision.REJECT,
                AdminReportAiTargetAction.APPROVE)).getResolutions(report.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getReportDecision()).isEqualTo(AdminReportAiReportDecision.REJECT);
    }

    private CapturingService serviceReturning(AdminReportAiResolutionWebhookResponseDTO response) {
        return new CapturingService(response, null);
    }

    private CapturingService serviceThrowing() {
        return new CapturingService(null, new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Admin report AI resolution service unavailable"));
    }

    private AdminReportAiResolutionWebhookResponseDTO response(
            AdminReportAiReportDecision reportDecision,
            AdminReportAiTargetAction targetAction) {
        AdminReportAiResolutionWebhookResponseDTO response = new AdminReportAiResolutionWebhookResponseDTO();
        response.setReportDecision(reportDecision);
        response.setTargetAction(targetAction);
        response.setConfidenceScore(92.5);
        response.setRiskScore(84.0);
        response.setLabels(List.of("policy-risk"));
        response.setRuleCode("BLOG_CLEAR_VIOLATION");
        response.setExplanation("Recommendation from n8n rule normalization");
        response.setModelName("gpt-4o-mini");
        response.setRawResponse(Map.of("source", "test"));
        return response;
    }

    private AdminReportAiResolution saved(AdminReportAiResolution resolution) {
        resolution.setId(UUID.randomUUID());
        resolution.setCreatedAt(LocalDateTime.now());
        return resolution;
    }

    private ContentReport blogReport() {
        ContentReport report = baseReport(ReportTargetType.BLOG);
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setContent("Reported blog content");
        blog.setStatus(PostStatus.PUBLISHED);
        blog.setImageUrls(List.of("https://example.com/blog.png"));
        report.setBlog(blog);
        return report;
    }

    private ContentReport commentReport() {
        ContentReport report = baseReport(ReportTargetType.COMMENT);
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setContent("Reported comment content");
        comment.setStatus(PostStatus.PUBLISHED);
        comment.setImageUrls(List.of("https://example.com/comment.png"));
        report.setComment(comment);
        return report;
    }

    private ContentReport userReport() {
        ContentReport report = baseReport(ReportTargetType.USER);
        User user = user("reported");
        user.setUserFullName("Reported User");
        user.setUserDescription("Profile text");
        user.setUserAvatar("https://example.com/avatar.png");
        user.setAccountStatus(true);
        report.setReportedUser(user);
        return report;
    }

    private ContentReport cafePageReport() {
        ContentReport report = baseReport(ReportTargetType.CAFE_PAGE);
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        cafePage.setName("Cafe Test");
        cafePage.setAddress("123 Street");
        cafePage.setDescription("Cafe description");
        cafePage.setAvatarUrl("https://example.com/page-avatar.png");
        cafePage.setCoverUrl("https://example.com/page-cover.png");
        cafePage.setStatus(PageStatus.ACTIVE);
        report.setCafePage(cafePage);
        return report;
    }

    private ContentReport baseReport(ReportTargetType targetType) {
        ContentReport report = new ContentReport();
        report.setId(UUID.randomUUID());
        report.setReporter(user("reporter"));
        report.setTargetType(targetType);
        report.setReason(reason());
        report.setReasonSnapshot("Spam or unsafe content");
        report.setDescription("Admin wants AI recommendation");
        report.setStatus(ReportStatus.OPEN);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    private ReportReason reason() {
        ReportReason reason = new ReportReason();
        reason.setId(UUID.randomUUID());
        reason.setCode("SCAM_FRAUD_OR_SPAM");
        reason.setLabelVi("Spam");
        reason.setSeverity(4);
        return reason;
    }

    private User user(String username) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName(username);
        user.setUserEmail(username + "@example.com");
        user.setUserPassword("password");
        return user;
    }

    private class CapturingService extends AdminReportAiResolutionServiceImpl {
        private final AdminReportAiResolutionWebhookResponseDTO response;
        private final RuntimeException exception;
        private AdminReportAiResolutionRequestDTO lastRequest;

        CapturingService(AdminReportAiResolutionWebhookResponseDTO response, RuntimeException exception) {
            super(
                    resolutionRepository,
                    contentReportRepository,
                    moderationResultRepository,
                    new ObjectMapper(),
                    null);
            this.response = response;
            this.exception = exception;
        }

        @Override
        protected AdminReportAiResolutionWebhookResponseDTO callWebhook(AdminReportAiResolutionRequestDTO request) {
            lastRequest = request;
            if (exception != null) {
                throw exception;
            }
            return response;
        }
    }
}
