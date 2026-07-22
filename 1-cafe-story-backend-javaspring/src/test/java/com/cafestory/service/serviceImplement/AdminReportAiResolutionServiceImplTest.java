package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiAutoApplyJobResponseDTO;
import com.cafestory.dto.responseDTO.AdminReportAiResolutionResponseDTO;
import com.cafestory.dto.responseDTO.AdminReportAiResolutionWebhookResponseDTO;
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
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.AdminReportAiResolutionRepository;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AdminReportAiResolutionServiceImplTest {

    private AdminReportAiResolutionRepository resolutionRepository;
    private ContentReportRepository contentReportRepository;
    private AiModerationResultRepository moderationResultRepository;
    private AdminReportAiAutoApplyJobService autoApplyJobService;

    @BeforeEach
    void setUp() {
        resolutionRepository = mock(AdminReportAiResolutionRepository.class);
        contentReportRepository = mock(ContentReportRepository.class);
        moderationResultRepository = mock(AiModerationResultRepository.class);
        autoApplyJobService = mock(AdminReportAiAutoApplyJobService.class);
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
    void createResolution_success_autoApplyRequestAttachesScheduledJob_TC001_1() {
        ContentReport report = blogReport();
        AdminReportAiAutoApplyJobResponseDTO jobResponse = new AdminReportAiAutoApplyJobResponseDTO();
        jobResponse.setId(UUID.randomUUID());
        jobResponse.setContentReportId(report.getId());
        CapturingService service = serviceReturning(response(
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                .thenReturn(Optional.empty());
        when(contentReportRepository.countByBlogIdAndStatusIn(any(UUID.class), any())).thenReturn(2L);
        when(resolutionRepository.save(any(AdminReportAiResolution.class)))
                .thenAnswer(invocation -> saved(invocation.getArgument(0)));
        when(autoApplyJobService.scheduleIfRequested(any(), any(), any(), any()))
                .thenReturn(new AdminReportAiAutoApplyJobService.ScheduleResult(jobResponse, null));
        com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO request =
                new com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO();
        request.setAutoApplyEnabled(true);
        request.setAutoApplyDelayMinutes(15);

        AdminReportAiResolutionResponseDTO result = service.createResolution(
                report.getId(),
                request,
                UUID.randomUUID());

        assertThat(result.getAutoApplyJob()).isEqualTo(jobResponse);
        assertThat(result.getAutoApplyWarning()).isNull();
        verify(autoApplyJobService).scheduleIfRequested(any(), any(), any(), any());
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
    void createResolution_fail_terminalReportDoesNotCallWebhook_TC005_1() {
        ContentReport report = blogReport();
        report.setStatus(ReportStatus.RESOLVED);
        CapturingService service = serviceReturning(response(
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.createResolution(report.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .contains("OPEN or REVIEWING"));

        assertThat(service.lastRequest).isNull();
        verify(resolutionRepository, never()).save(any(AdminReportAiResolution.class));
    }

    @Test
    void createResolution_fail_reportTargetUnavailable_TC005_2() {
        ContentReport report = baseReport(ReportTargetType.BLOG);
        CapturingService service = serviceReturning(response(
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.createResolution(report.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .contains("target is unavailable"));

        assertThat(service.lastRequest).isNull();
    }

    @Test
    void createResolution_fail_inconsistentDecisionActionDoesNotSave_TC005_3() {
        ContentReport report = blogReport();
        AdminReportAiResolutionWebhookResponseDTO invalid = response(
                AdminReportAiReportDecision.REJECT,
                AdminReportAiTargetAction.HIDE);
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceReturning(invalid).createResolution(report.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .contains("inconsistent"));

        verify(resolutionRepository, never()).save(any(AdminReportAiResolution.class));
    }

    @Test
    void createResolution_fail_invalidScoresOrAuditMetadataDoesNotSave_TC005_4() {
        ContentReport report = blogReport();
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                .thenReturn(Optional.empty());

        AdminReportAiResolutionWebhookResponseDTO invalidScore = response(
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE);
        invalidScore.setConfidenceScore(Double.NaN);
        assertThatThrownBy(() -> serviceReturning(invalidScore).createResolution(report.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .contains("scores"));

        AdminReportAiResolutionWebhookResponseDTO missingAudit = response(
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE);
        missingAudit.setModelName(" ");
        assertThatThrownBy(() -> serviceReturning(missingAudit).createResolution(report.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .contains("audit metadata"));

        verify(resolutionRepository, never()).save(any(AdminReportAiResolution.class));
    }

    @Test
    void createResolution_success_rejectUsesNonDestructiveActionForTarget_TC005_5() {
        List<ContentReport> reports = List.of(blogReport(), userReport());
        List<AdminReportAiTargetAction> actions = List.of(
                AdminReportAiTargetAction.APPROVE,
                AdminReportAiTargetAction.KEEP_ACTIVE);

        for (int index = 0; index < reports.size(); index++) {
            ContentReport report = reports.get(index);
            when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
            when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                    .thenReturn(Optional.empty());
            when(resolutionRepository.save(any(AdminReportAiResolution.class)))
                    .thenAnswer(invocation -> saved(invocation.getArgument(0)));

            AdminReportAiResolutionResponseDTO result = serviceReturning(response(
                    AdminReportAiReportDecision.REJECT,
                    actions.get(index))).createResolution(report.getId());

            assertThat(result.getTargetAction()).isEqualTo(actions.get(index));
        }
    }

    @Test
    void callWebhook_success_parsesJsonResponse_TC005_6() throws Exception {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AdminReportAiResolutionServiceImpl service = serviceWithRestClient(builder.build());
        AdminReportAiResolutionWebhookResponseDTO expected = response(
                AdminReportAiReportDecision.REJECT,
                AdminReportAiTargetAction.APPROVE);
        server.expect(requestTo("http://localhost"))
                .andRespond(withSuccess(new ObjectMapper().writeValueAsString(expected), MediaType.APPLICATION_JSON));

        AdminReportAiResolutionWebhookResponseDTO actual = service.callWebhook(webhookRequest());

        assertThat(actual.getReportDecision()).isEqualTo(AdminReportAiReportDecision.REJECT);
        assertThat(actual.getTargetAction()).isEqualTo(AdminReportAiTargetAction.APPROVE);
        server.verify();
    }

    @Test
    void callWebhook_fail_non2xxEmptyOrInvalidJson_TC005_7() {
        assertWebhookFailure(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        assertWebhookFailure(withSuccess("", MediaType.APPLICATION_JSON));
        assertWebhookFailure(withSuccess("not-json", MediaType.TEXT_PLAIN));
    }

    @Test
    void constructor_success_buildsConfiguredRestClient_TC005_8() {
        AdminReportAiResolutionServiceImpl service = new AdminReportAiResolutionServiceImpl(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                autoApplyJobService,
                new ObjectMapper(),
                "http://localhost:5678/webhook/cafestory-admin-report-ai-resolution",
                40_000);

        assertThat(service).isNotNull();
    }

    @Test
    void getResolutions_fail_nullReportId_TC005_9() {
        assertThatThrownBy(() -> serviceReturning(response(
                AdminReportAiReportDecision.REJECT,
                AdminReportAiTargetAction.APPROVE)).getResolutions(null, PageRequest.of(0, 20)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createResolution_success_includesExistingModerationAndNullOptionalValues_TC005_10() {
        ContentReport report = blogReport();
        report.setDescription(null);
        report.getBlog().setImageUrls(null);
        AiModerationResult moderation = new AiModerationResult();
        moderation.setId(UUID.randomUUID());
        moderation.setScore(91.0);
        moderation.setDecision(com.cafestory.entity.enums.ModerationDecision.VIOLATION);
        moderation.setTags(List.of("spam"));
        moderation.setResolved(false);
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                .thenReturn(Optional.of(moderation));
        when(resolutionRepository.save(any(AdminReportAiResolution.class)))
                .thenAnswer(invocation -> saved(invocation.getArgument(0)));
        AdminReportAiResolutionWebhookResponseDTO webhookResponse = response(
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE);
        webhookResponse.setLabels(null);
        webhookResponse.setRawResponse(null);
        webhookResponse.setRuleCode(" ");
        CapturingService service = serviceReturning(webhookResponse);

        AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

        assertThat(service.lastRequest.getExistingModerationResult())
                .containsEntry("decision", com.cafestory.entity.enums.ModerationDecision.VIOLATION)
                .containsEntry("score", 91.0);
        assertThat(service.lastRequest.getImageUrls()).isEmpty();
        assertThat(result.getLabels()).isEmpty();
        assertThat(result.getRuleCode()).isNull();
        assertThat(result.getRawResponse()).containsEntry("reportDecision", "RESOLVE");
    }

    @Test
    void createResolution_success_coversAllValidDecisionActionCombinations_TC005_11() {
        List<RecommendationCase> cases = List.of(
                new RecommendationCase(blogReport(), AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW, AdminReportAiTargetAction.NONE),
                new RecommendationCase(blogReport(), AdminReportAiReportDecision.REJECT, AdminReportAiTargetAction.APPROVE),
                new RecommendationCase(blogReport(), AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.REMOVE),
                new RecommendationCase(commentReport(), AdminReportAiReportDecision.REJECT, AdminReportAiTargetAction.APPROVE),
                new RecommendationCase(commentReport(), AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE),
                new RecommendationCase(userReport(), AdminReportAiReportDecision.REJECT, AdminReportAiTargetAction.KEEP_ACTIVE),
                new RecommendationCase(userReport(), AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.SUSPEND_USER),
                new RecommendationCase(cafePageReport(), AdminReportAiReportDecision.REJECT, AdminReportAiTargetAction.KEEP_ACTIVE),
                new RecommendationCase(cafePageReport(), AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.SUSPEND_PAGE));

        for (RecommendationCase recommendationCase : cases) {
            ContentReport report = recommendationCase.report();
            when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
            when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                    .thenReturn(Optional.empty());
            when(resolutionRepository.save(any(AdminReportAiResolution.class)))
                    .thenAnswer(invocation -> saved(invocation.getArgument(0)));

            AdminReportAiResolutionResponseDTO result = serviceReturning(response(
                    recommendationCase.decision(),
                    recommendationCase.action())).createResolution(report.getId());

            assertThat(result.getReportDecision()).isEqualTo(recommendationCase.decision());
            assertThat(result.getTargetAction()).isEqualTo(recommendationCase.action());
        }
    }

    @Test
    void createResolution_fail_targetActionFromAnotherTargetType_TC005_12() {
        for (RecommendationCase invalidCase : List.of(
                new RecommendationCase(blogReport(), AdminReportAiReportDecision.REJECT, AdminReportAiTargetAction.KEEP_ACTIVE),
                new RecommendationCase(userReport(), AdminReportAiReportDecision.REJECT, AdminReportAiTargetAction.APPROVE),
                new RecommendationCase(cafePageReport(), AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE))) {
            ContentReport report = invalidCase.report();
            when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
            when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceReturning(response(
                    invalidCase.decision(),
                    invalidCase.action())).createResolution(report.getId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                            .contains("targetAction"));
        }
    }

    @Test
    void createResolution_fail_allInvalidScoreShapes_TC005_13() {
        ContentReport report = blogReport();
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                .thenReturn(Optional.empty());

        for (Double invalid : java.util.Arrays.asList(null, Double.NaN, Double.POSITIVE_INFINITY, -1.0, 101.0)) {
            AdminReportAiResolutionWebhookResponseDTO invalidConfidence = response(
                    AdminReportAiReportDecision.RESOLVE,
                    AdminReportAiTargetAction.HIDE);
            invalidConfidence.setConfidenceScore(invalid);
            assertThatThrownBy(() -> serviceReturning(invalidConfidence).createResolution(report.getId()))
                    .isInstanceOf(ResponseStatusException.class);

            AdminReportAiResolutionWebhookResponseDTO invalidRisk = response(
                    AdminReportAiReportDecision.RESOLVE,
                    AdminReportAiTargetAction.HIDE);
            invalidRisk.setRiskScore(invalid);
            assertThatThrownBy(() -> serviceReturning(invalidRisk).createResolution(report.getId()))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    @Test
    void createResolution_fail_missingResponseDecisionActionOrAuditFields_TC005_14() {
        ContentReport report = blogReport();
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                .thenReturn(Optional.empty());

        AdminReportAiResolutionWebhookResponseDTO missingAction = response(
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE);
        missingAction.setTargetAction(null);
        AdminReportAiResolutionWebhookResponseDTO missingExplanation = response(
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE);
        missingExplanation.setExplanation(null);
        AdminReportAiResolutionWebhookResponseDTO missingModel = response(
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE);
        missingModel.setModelName(null);

        for (AdminReportAiResolutionWebhookResponseDTO invalid : java.util.Arrays.asList(
                null,
                new AdminReportAiResolutionWebhookResponseDTO(),
                missingAction,
                missingExplanation,
                missingModel)) {
            assertThatThrownBy(() -> serviceReturning(invalid).createResolution(report.getId()))
                    .isInstanceOf(ResponseStatusException.class);
        }
    }

    @Test
    void createResolution_success_handlesBlankOptionalTargetMetadata_TC005_15() {
        ContentReport commentReport = commentReport();
        commentReport.getComment().setImageUrls(null);
        ContentReport userReport = userReport();
        userReport.getReportedUser().setUserName(null);
        userReport.getReportedUser().setUserFullName(null);
        userReport.getReportedUser().setUserDescription(null);
        userReport.getReportedUser().setUserAvatar(null);
        ContentReport pageReport = cafePageReport();
        pageReport.getCafePage().setName(null);
        pageReport.getCafePage().setAddress(null);
        pageReport.getCafePage().setDescription(null);
        pageReport.getCafePage().setAvatarUrl(null);
        pageReport.getCafePage().setCoverUrl(null);

        for (ContentReport report : List.of(commentReport, userReport, pageReport)) {
            when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
            when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                    .thenReturn(Optional.empty());
            when(resolutionRepository.save(any(AdminReportAiResolution.class)))
                    .thenAnswer(invocation -> saved(invocation.getArgument(0)));
            CapturingService service = serviceReturning(response(
                    AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW,
                    AdminReportAiTargetAction.NONE));

            service.createResolution(report.getId());

            assertThat(service.lastRequest.getImageUrls()).isEmpty();
        }
    }

    @Test
    void getResolutions_success_handlesLegacyResolutionWithoutReportReference_TC005_16() {
        ContentReport report = blogReport();
        AdminReportAiResolution resolution = saved(new AdminReportAiResolution());
        resolution.setContentReport(null);
        resolution.setTargetType(ReportTargetType.BLOG);
        resolution.setTargetId(report.getBlog().getId());
        resolution.setReportDecision(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        resolution.setTargetAction(AdminReportAiTargetAction.NONE);
        PageRequest pageable = PageRequest.of(0, 20);
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(resolutionRepository.findByContentReportId(report.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(resolution)));

        AdminReportAiResolutionResponseDTO result = serviceReturning(response(
                AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW,
                AdminReportAiTargetAction.NONE)).getResolutions(report.getId(), pageable).getContent().getFirst();

        assertThat(result.getContentReportId()).isNull();
    }

    private void assertWebhookFailure(org.springframework.test.web.client.ResponseCreator responseCreator) {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://localhost")).andRespond(responseCreator);

        assertThatThrownBy(() -> serviceWithRestClient(builder.build()).callWebhook(webhookRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
        server.verify();
    }

    private AdminReportAiResolutionServiceImpl serviceWithRestClient(RestClient restClient) {
        return new AdminReportAiResolutionServiceImpl(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                autoApplyJobService,
                new ObjectMapper(),
                restClient);
    }

    private AdminReportAiResolutionRequestDTO webhookRequest() {
        return new AdminReportAiResolutionRequestDTO(
                UUID.randomUUID(),
                ReportTargetType.BLOG,
                UUID.randomUUID(),
                "TEST",
                "Test",
                1,
                "description",
                "content",
                List.of(),
                ReportStatus.OPEN,
                null,
                1L);
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
                    autoApplyJobService,
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

    private record RecommendationCase(
            ContentReport report,
            AdminReportAiReportDecision decision,
            AdminReportAiTargetAction action) {
    }
}
