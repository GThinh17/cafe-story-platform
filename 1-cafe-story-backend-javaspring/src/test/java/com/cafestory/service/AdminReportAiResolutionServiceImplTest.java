package com.cafestory.service;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiAutoApplyJobResponseDTO;
import com.cafestory.dto.responseDTO.AdminReportAiResolutionResponseDTO;
import com.cafestory.entity.AdminReportAiResolution;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Comment;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.ReportReason;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.AdminReportAiResolutionRepository;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.service.serviceImplement.AdminReportAiResolutionServiceImpl;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kiểm thử {@link AdminReportAiResolutionServiceImpl}.
 *
 * <p>Webhook n8n được thay bằng một {@link HttpServer} trong tiến trình, nên
 * bài kiểm thử điều khiển được cả nội dung lẫn mã trạng thái mà "AI" trả về —
 * kể cả các trường hợp hỏng: 5xx, thân rỗng, JSON sai định dạng.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminReportAiResolutionServiceImplTest {

    @Mock
    private AdminReportAiResolutionRepository resolutionRepository;
    @Mock
    private ContentReportRepository contentReportRepository;
    @Mock
    private AiModerationResultRepository moderationResultRepository;
    @Mock
    private AdminReportAiAutoApplyJobService autoApplyJobService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<String> webhookBody = new AtomicReference<>("{}");
    private final AtomicInteger webhookStatus = new AtomicInteger(200);
    private final AtomicReference<String> requestBody = new AtomicReference<>();

    private HttpServer webhookServer;
    private AdminReportAiResolutionServiceImpl resolutionService;

    @BeforeEach
    void setUp() throws IOException {
        webhookServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        webhookServer.createContext("/webhook", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] payload = webhookBody.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(webhookStatus.get(), payload.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(payload);
            }
        });
        webhookServer.start();

        resolutionService = new AdminReportAiResolutionServiceImpl(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                autoApplyJobService,
                objectMapper,
                "http://127.0.0.1:" + webhookServer.getAddress().getPort() + "/webhook",
                5_000);

        when(resolutionRepository.save(any(AdminReportAiResolution.class))).thenAnswer(invocation -> {
            AdminReportAiResolution saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });
    }

    @AfterEach
    void tearDown() {
        webhookServer.stop(0);
    }

    @Test
    void createResolution_success_blogReportPersistsAiVerdict_TC001() {
        ContentReport report = blogReport();
        givenReport(report);
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                .thenReturn(Optional.of(moderationResult()));
        when(contentReportRepository.countByBlogIdAndStatusIn(any(UUID.class), any())).thenReturn(3L);
        webhookBody.set("""
                {
                  "reportDecision": "RESOLVE",
                  "targetAction": "HIDE",
                  "confidenceScore": 88.5,
                  "riskScore": 70.0,
                  "labels": ["spam"],
                  "ruleCode": "R-12",
                  "explanation": "Noi dung quang cao",
                  "modelName": "gpt-4o"
                }
                """);

        AdminReportAiResolutionResponseDTO result = resolutionService.createResolution(report.getId());

        assertThat(result.getReportDecision()).isEqualTo(AdminReportAiReportDecision.RESOLVE);
        assertThat(result.getTargetAction()).isEqualTo(AdminReportAiTargetAction.HIDE);
        assertThat(result.getConfidenceScore()).isEqualTo(88.5);
        assertThat(result.getLabels()).containsExactly("spam");
        assertThat(result.getRuleCode()).isEqualTo("R-12");
        assertThat(result.getModelName()).isEqualTo("gpt-4o");
        assertThat(result.getTargetId()).isEqualTo(report.getBlog().getId());
        assertThat(result.getRawResponse()).containsEntry("reportDecision", "RESOLVE");
        assertThat(result.getAutoApplyJob()).isNull();
        // Yêu cầu gửi sang n8n phải mang đủ ngữ cảnh: nội dung, ảnh và số báo cáo cùng đích.
        assertThat(requestBody.get()).contains("Quan yen tinh");
        assertThat(requestBody.get()).contains("https://cdn.example.com/1.png");
        assertThat(requestBody.get()).contains("\"sameTargetOpenReportCount\":3");
    }

    @Test
    void createResolution_success_schedulesAutoApplyWhenRequested_TC002() {
        ContentReport report = blogReport();
        givenReport(report);
        AdminReportAiResolutionCreateRequestDTO request = new AdminReportAiResolutionCreateRequestDTO();
        request.setAutoApplyEnabled(true);
        request.setAutoApplyDelayMinutes(30);
        UUID adminUserId = UUID.randomUUID();
        AdminReportAiAutoApplyJobResponseDTO job = new AdminReportAiAutoApplyJobResponseDTO();
        when(autoApplyJobService.scheduleIfRequested(
                any(ContentReport.class), any(AdminReportAiResolution.class),
                any(AdminReportAiResolutionCreateRequestDTO.class), any(UUID.class)))
                .thenReturn(new AdminReportAiAutoApplyJobService.ScheduleResult(job, "do tin cay thap"));
        webhookBody.set(verdict("RESOLVE", "REMOVE"));

        AdminReportAiResolutionResponseDTO result =
                resolutionService.createResolution(report.getId(), request, adminUserId);

        assertThat(result.getAutoApplyJob()).isSameAs(job);
        assertThat(result.getAutoApplyWarning()).isEqualTo("do tin cay thap");
    }

    @Test
    void createResolution_success_autoApplyDisabledSkipsScheduling_TC003() {
        ContentReport report = blogReport();
        givenReport(report);
        AdminReportAiResolutionCreateRequestDTO request = new AdminReportAiResolutionCreateRequestDTO();
        request.setAutoApplyEnabled(false);
        webhookBody.set(verdict("REJECT", "APPROVE"));

        resolutionService.createResolution(report.getId(), request, UUID.randomUUID());

        verify(autoApplyJobService, never()).scheduleIfRequested(any(), any(), any(), any());
    }

    @Test
    void createResolution_success_commentReportUsesCommentContent_TC004() {
        ContentReport report = commentReport();
        givenReport(report);
        when(contentReportRepository.countByCommentIdAndStatusIn(any(UUID.class), any())).thenReturn(1L);
        webhookBody.set(verdict("RESOLVE", "REMOVE"));

        AdminReportAiResolutionResponseDTO result = resolutionService.createResolution(report.getId());

        assertThat(result.getTargetId()).isEqualTo(report.getComment().getId());
        assertThat(requestBody.get()).contains("Binh luan xau");
        assertThat(requestBody.get()).contains("https://cdn.example.com/comment.png");
    }

    @Test
    void createResolution_success_userReportBuildsProfileText_TC005() {
        ContentReport report = userReport();
        givenReport(report);
        when(contentReportRepository.countByReportedUserUserIdAndStatusIn(any(UUID.class), any())).thenReturn(2L);
        webhookBody.set(verdict("RESOLVE", "SUSPEND_USER"));

        AdminReportAiResolutionResponseDTO result = resolutionService.createResolution(report.getId());

        assertThat(result.getTargetId()).isEqualTo(report.getReportedUser().getUserId());
        assertThat(requestBody.get()).contains("username: kesau");
        assertThat(requestBody.get()).contains("fullName: Ke Sau");
        assertThat(requestBody.get()).contains("https://cdn.example.com/kesau.png");
    }

    @Test
    void createResolution_success_cafePageReportBuildsPageText_TC006() {
        ContentReport report = cafePageReport();
        givenReport(report);
        when(contentReportRepository.countByCafePageIdAndStatusIn(any(UUID.class), any())).thenReturn(4L);
        webhookBody.set(verdict("RESOLVE", "SUSPEND_PAGE"));

        AdminReportAiResolutionResponseDTO result = resolutionService.createResolution(report.getId());

        assertThat(result.getTargetId()).isEqualTo(report.getCafePage().getId());
        assertThat(requestBody.get()).contains("name: Quan xau");
        assertThat(requestBody.get()).contains("address: 30/4");
        assertThat(requestBody.get()).contains("https://cdn.example.com/page-avatar.png");
        assertThat(requestBody.get()).contains("https://cdn.example.com/page-cover.png");
    }

    @Test
    void createResolution_success_reportWithoutTargetEntity_TC007() {
        ContentReport report = blogReport();
        report.setBlog(null);
        report.setReason(null);
        givenReport(report);
        webhookBody.set(verdict("NEEDS_MANUAL_REVIEW", "NONE"));

        AdminReportAiResolutionResponseDTO result = resolutionService.createResolution(report.getId());

        assertThat(result.getTargetId()).isNull();
        assertThat(requestBody.get()).contains("\"sameTargetOpenReportCount\":0");
    }

    @Test
    void createResolution_success_normalizesOutOfRangeAndMissingScores_TC008() {
        ContentReport report = blogReport();
        givenReport(report);
        webhookBody.set("""
                {"reportDecision":"RESOLVE","targetAction":"APPROVE","confidenceScore":250.0,"riskScore":-30.0}
                """);

        AdminReportAiResolutionResponseDTO result = resolutionService.createResolution(report.getId());

        assertThat(result.getConfidenceScore()).isEqualTo(100.0);
        assertThat(result.getRiskScore()).isZero();
        assertThat(result.getLabels()).isEmpty();
        assertThat(result.getRuleCode()).isNull();
        assertThat(result.getExplanation()).isNull();
        // modelName để trống thì dùng mặc định của hệ thống.
        assertThat(result.getModelName()).isEqualTo("gpt-4o-mini");
    }

    @Test
    void createResolution_fail_reportIdMissing_TC009() {
        assertThatThrownBy(() -> resolutionService.createResolution(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Report id is required");
    }

    @Test
    void createResolution_fail_reportNotFound_TC010() {
        UUID reportId = UUID.randomUUID();
        when(contentReportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolutionService.createResolution(reportId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Report not found");
    }

    @Test
    void createResolution_fail_webhookReturnsServerError_TC011() {
        ContentReport report = blogReport();
        UUID reportId = report.getId();
        givenReport(report);
        webhookStatus.set(500);

        assertThatThrownBy(() -> resolutionService.createResolution(reportId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Admin report AI resolution service unavailable")
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void createResolution_fail_webhookReturnsEmptyBody_TC012() {
        ContentReport report = blogReport();
        UUID reportId = report.getId();
        givenReport(report);
        webhookBody.set("   ");

        assertThatThrownBy(() -> resolutionService.createResolution(reportId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Admin report AI resolution response is empty");
    }

    @Test
    void createResolution_fail_webhookReturnsInvalidJson_TC013() {
        ContentReport report = blogReport();
        UUID reportId = report.getId();
        givenReport(report);
        webhookBody.set("<html>khong phai json</html>");

        assertThatThrownBy(() -> resolutionService.createResolution(reportId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Admin report AI resolution response is not valid JSON");
    }

    @Test
    void createResolution_fail_responseMissingRequiredFields_TC014() {
        ContentReport report = blogReport();
        UUID reportId = report.getId();
        givenReport(report);
        webhookBody.set("{\"reportDecision\":\"RESOLVE\"}");

        assertThatThrownBy(() -> resolutionService.createResolution(reportId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("missing required fields");
    }

    @Test
    void createResolution_fail_targetActionDoesNotMatchTargetType_TC015() {
        ContentReport report = blogReport();
        UUID reportId = report.getId();
        givenReport(report);
        // SUSPEND_USER không hợp lệ cho báo cáo bài viết.
        webhookBody.set(verdict("RESOLVE", "SUSPEND_USER"));

        assertThatThrownBy(() -> resolutionService.createResolution(reportId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("targetAction is not valid for report target type");
    }

    @Test
    void createResolution_success_noneActionIsAlwaysAllowed_TC016() {
        ContentReport report = userReport();
        givenReport(report);
        webhookBody.set(verdict("NEEDS_MANUAL_REVIEW", "NONE"));

        assertThat(resolutionService.createResolution(report.getId()).getTargetAction())
                .isEqualTo(AdminReportAiTargetAction.NONE);
    }

    @Test
    void createResolution_fail_cafePageActionRejectedForUserReport_TC017() {
        ContentReport report = userReport();
        UUID reportId = report.getId();
        givenReport(report);
        webhookBody.set(verdict("RESOLVE", "SUSPEND_PAGE"));

        assertThatThrownBy(() -> resolutionService.createResolution(reportId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("targetAction is not valid");
    }

    @Test
    void createResolution_success_keepsProvidedRawResponse_TC018() {
        ContentReport report = blogReport();
        givenReport(report);
        webhookBody.set("""
                {"reportDecision":"REJECT","targetAction":"APPROVE","rawResponse":{"tokens":120}}
                """);

        assertThat(resolutionService.createResolution(report.getId()).getRawResponse())
                .containsEntry("tokens", 120);
    }

    @Test
    void getResolutions_success_mapsPage_TC019() {
        ContentReport report = blogReport();
        givenReport(report);
        PageRequest pageable = PageRequest.of(0, 10);
        AdminReportAiResolution resolution = new AdminReportAiResolution();
        resolution.setId(UUID.randomUUID());
        resolution.setContentReport(report);
        resolution.setTargetType(ReportTargetType.BLOG);
        resolution.setReportDecision(AdminReportAiReportDecision.RESOLVE);
        resolution.setTargetAction(AdminReportAiTargetAction.HIDE);
        when(resolutionRepository.findByContentReportId(report.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(resolution)));

        Page<AdminReportAiResolutionResponseDTO> result =
                resolutionService.getResolutions(report.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContentReportId()).isEqualTo(report.getId());
    }

    @Test
    void getResolutions_fail_reportNotFound_TC020() {
        UUID reportId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        when(contentReportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolutionService.getResolutions(reportId, pageable))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Report not found");
    }

    @Test
    void createResolution_success_resolutionWithoutReportMapsNullReportId_TC021() {
        ContentReport report = blogReport();
        givenReport(report);
        PageRequest pageable = PageRequest.of(0, 10);
        AdminReportAiResolution orphan = new AdminReportAiResolution();
        orphan.setId(UUID.randomUUID());
        when(resolutionRepository.findByContentReportId(report.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(orphan)));

        assertThat(resolutionService.getResolutions(report.getId(), pageable)
                .getContent().get(0).getContentReportId()).isNull();
    }

    // ------------------------------------------------------------- Helpers

    @Test
    void createResolution_success_reportTargetsMissingOnEveryType_TC022() {
        for (ReportTargetType targetType : ReportTargetType.values()) {
            ContentReport report = baseReport(targetType);
            givenReport(report);
            webhookBody.set(verdict("NEEDS_MANUAL_REVIEW", "NONE"));

            AdminReportAiResolutionResponseDTO result = resolutionService.createResolution(report.getId());

            assertThat(result.getTargetId()).isNull();
            assertThat(requestBody.get()).contains("\"sameTargetOpenReportCount\":0");
        }
    }

    @Test
    void createResolution_success_keepActiveIsAllowedForUserAndCafePage_TC023() {
        ContentReport userReport = userReport();
        givenReport(userReport);
        webhookBody.set(verdict("RESOLVE", "KEEP_ACTIVE"));
        assertThat(resolutionService.createResolution(userReport.getId()).getTargetAction())
                .isEqualTo(AdminReportAiTargetAction.KEEP_ACTIVE);

        ContentReport cafePageReport = cafePageReport();
        givenReport(cafePageReport);
        assertThat(resolutionService.createResolution(cafePageReport.getId()).getTargetAction())
                .isEqualTo(AdminReportAiTargetAction.KEEP_ACTIVE);
    }

    @Test
    void createResolution_success_commentActionsAreAllowed_TC024() {
        ContentReport report = commentReport();
        givenReport(report);
        webhookBody.set(verdict("RESOLVE", "APPROVE"));

        assertThat(resolutionService.createResolution(report.getId()).getTargetAction())
                .isEqualTo(AdminReportAiTargetAction.APPROVE);
    }

    @Test
    void createResolution_success_autoApplyServiceMissingSkipsScheduling_TC025() {
        AdminReportAiResolutionServiceImpl withoutJobService = new AdminReportAiResolutionServiceImpl(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                null,
                objectMapper,
                "http://127.0.0.1:" + webhookServer.getAddress().getPort() + "/webhook",
                5_000);
        ContentReport report = blogReport();
        givenReport(report);
        AdminReportAiResolutionCreateRequestDTO request = new AdminReportAiResolutionCreateRequestDTO();
        request.setAutoApplyEnabled(true);
        request.setAutoApplyDelayMinutes(30);
        webhookBody.set(verdict("RESOLVE", "HIDE"));

        assertThat(withoutJobService.createResolution(report.getId(), request, UUID.randomUUID())
                .getAutoApplyJob()).isNull();
    }

    private void givenReport(ContentReport report) {
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
    }

    private String verdict(String reportDecision, String targetAction) {
        return "{\"reportDecision\":\"" + reportDecision + "\",\"targetAction\":\"" + targetAction + "\"}";
    }

    private ContentReport baseReport(ReportTargetType targetType) {
        ReportReason reason = new ReportReason();
        reason.setCode("SPAM");
        reason.setSeverity(3);

        ContentReport report = new ContentReport();
        report.setId(UUID.randomUUID());
        report.setTargetType(targetType);
        report.setReason(reason);
        report.setReasonSnapshot("Spam quang cao");
        report.setDescription("Bai dang lien tuc quang cao");
        report.setStatus(ReportStatus.OPEN);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    private ContentReport blogReport() {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setContent("Quan yen tinh, cafe sua ngon.");
        blog.setImageUrls(List.of("https://cdn.example.com/1.png"));
        ContentReport report = baseReport(ReportTargetType.BLOG);
        report.setBlog(blog);
        return report;
    }

    private ContentReport commentReport() {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setContent("Binh luan xau");
        comment.setImageUrls(List.of("https://cdn.example.com/comment.png"));
        ContentReport report = baseReport(ReportTargetType.COMMENT);
        report.setComment(comment);
        return report;
    }

    private ContentReport userReport() {
        User reported = new User();
        reported.setUserId(UUID.randomUUID());
        reported.setUserName("kesau");
        reported.setUserFullName("Ke Sau");
        reported.setUserDescription("Mo ta");
        reported.setUserAvatar("https://cdn.example.com/kesau.png");
        ContentReport report = baseReport(ReportTargetType.USER);
        report.setReportedUser(reported);
        return report;
    }

    private ContentReport cafePageReport() {
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        cafePage.setName("Quan xau");
        cafePage.setAddress("30/4");
        cafePage.setDescription("Mo ta quan");
        cafePage.setAvatarUrl("https://cdn.example.com/page-avatar.png");
        cafePage.setCoverUrl("https://cdn.example.com/page-cover.png");
        ContentReport report = baseReport(ReportTargetType.CAFE_PAGE);
        report.setCafePage(cafePage);
        return report;
    }

    private AiModerationResult moderationResult() {
        AiModerationResult result = new AiModerationResult();
        result.setId(UUID.randomUUID());
        result.setDecision(ModerationDecision.NEEDS_REVIEW);
        result.setScore(0.8);
        result.setTags(List.of("spam"));
        result.setExplanation("Nghi ngo quang cao");
        result.setAiStatus("DONE");
        result.setPriorityScore(0.5);
        result.setRiskScore(0.6);
        result.setResolved(false);
        return result;
    }
}
