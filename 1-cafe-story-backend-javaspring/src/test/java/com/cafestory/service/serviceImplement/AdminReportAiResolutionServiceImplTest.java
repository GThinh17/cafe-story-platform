package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiEvidenceItemRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiPolicyContextRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiResolutionRequestDTO;
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
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.exception.AdminReportAiProviderBoundaryException;
import com.cafestory.repository.AdminReportAiResolutionRepository;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.mockito.MockedStatic;

class AdminReportAiResolutionServiceImplTest {

    private AdminReportAiResolutionRepository resolutionRepository;
    private ContentReportRepository contentReportRepository;
    private AiModerationResultRepository moderationResultRepository;
    private AdminReportAiAutoApplyJobService autoApplyJobService;
    private ObjectMapper objectMapper;
    private AdminReportAiWebhookSigner webhookSigner;
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        resolutionRepository = mock(AdminReportAiResolutionRepository.class);
        contentReportRepository = mock(ContentReportRepository.class);
        moderationResultRepository = mock(AiModerationResultRepository.class);
        autoApplyJobService = mock(AdminReportAiAutoApplyJobService.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();
        webhookSigner = new AdminReportAiWebhookSigner(objectMapper, "unit-test-secret", 120, 300, Clock.systemUTC());
        entityManager = mock(EntityManager.class);
        when(resolutionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(resolutionRepository.save(any(AdminReportAiResolution.class))).thenAnswer(invocation -> {
            AdminReportAiResolution resolution = invocation.getArgument(0);
            resolution.setId(UUID.randomUUID());
            resolution.setCreatedAt(LocalDateTime.now());
            return resolution;
        });
        when(autoApplyJobService.scheduleIfRequested(any(), any(), any(), any()))
                .thenReturn(new AdminReportAiAutoApplyJobService.ScheduleResult(null, null));
    }

    @Test
    void createResolution_validBlogV2PersistsSanitizedEvidenceFirstRecord_TC001() {
        ContentReport report = blogReport("Reported blog content");
        stubReport(report);
        AtomicReference<AdminReportAiResolution> saved = captureSavedResolution();
        CapturingService service = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));

        AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

        assertThat(result.getContractVersion()).isEqualTo("2.0");
        assertThat(result.getAutomationMode()).isEqualTo("A0_RECOMMEND_ONLY");
        assertThat(result.getReportDecision()).isEqualTo(AdminReportAiReportDecision.RESOLVE);
        assertThat(result.getTargetAction()).isEqualTo(AdminReportAiTargetAction.HIDE);
        assertThat(result.getFindings()).hasSize(2);
        assertThat(result.getEvidenceSufficiency()).isEqualTo("SUFFICIENT");
        assertThat(result.getTargetSnapshotHash()).startsWith("sha256:").hasSize(71);
        assertThat(saved.get().getRawResponse()).isNull();
        assertThat(saved.get().getConfidenceScore()).isNull();
        assertThat(saved.get().getRiskScore()).isNull();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.OPEN);
        assertThat(report.getBlog().getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(service.lastRequest.getPolicyContext().getContextSchemaVersion())
                .isEqualTo(AdminReportAiPolicyCatalog.CONTEXT_SCHEMA_VERSION);
        assertThat(service.lastRequest.getPolicyContext().getCandidateRules()).isNotEmpty();
        assertThat(service.lastRequest.getEvidence())
                .extracting(AdminReportAiEvidenceItemRequestDTO::getEvidenceId)
                .contains("EV-TARGET-IDENTITY", "EV-TARGET-CONTENT", "EV-TARGET-STATE")
                .doesNotContain("EV-REASON-ROUTE", "EV-DERIVED-MODERATION");
        verify(contentReportRepository, never()).save(any(ContentReport.class));
    }

    @Test
    void toWebhookRequest_buildsActiveRuntimeTypedRuleContextAndSnapshotBoundEvidence_S201_TC001() {
        ContentReport report = blogReport("Bounded evidence text");
        CapturingService service = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));

        AdminReportAiResolutionRequestDTO request =
                ReflectionTestUtils.invokeMethod(service, "toWebhookRequest", report);

        assertThat(request.getPolicyContext().getPolicyStatus()).isEqualTo("ACTIVE");
        assertThat(request.getPolicyContext().getRuleCatalogStatus()).isEqualTo("ACTIVE");
        assertThat(request.getPolicyContext().getEvaluationMode()).isEqualTo("ACTIVE_RUNTIME");
        assertThat(request.getPolicyContext().getCurrentEvaluationCeiling())
                .isEqualTo("RESOLVE_OR_REJECT");
        assertThat(request.getPolicyContext().getCandidateRules())
                .allSatisfy(rule -> {
                    assertThat(rule.getRuleId()).isNotBlank();
                    assertThat(rule.getRuleStatus()).isEqualTo("ACTIVE");
                    assertThat(rule.getRequiredEvidenceKinds()).isNotEmpty();
                    assertThat(rule.getAllowedCandidateActions())
                            .contains("NO_ACTION", "KEEP_VISIBLE");
                });
        assertThat(request.getEvidence())
                .extracting(AdminReportAiEvidenceItemRequestDTO::getEvidenceId)
                .doesNotHaveDuplicates();
        assertThat(request.getEvidence())
                .allSatisfy(item -> {
                    assertThat(item.getEnvelopeVersion())
                            .isEqualTo(AdminReportAiPolicyCatalog.EVIDENCE_ENVELOPE_VERSION);
                    assertThat(item.getSubject().getTargetType()).isEqualTo(request.getTargetType());
                    assertThat(item.getSubject().getSnapshotHash())
                            .isEqualTo(request.getTargetSnapshot().get("snapshotHash"));
                    assertThat(item.getPayload()).isNotNull();
                    assertThat(item.getIntegrity().getCanonicalization()).isEqualTo("JCS");
                });
    }

    @Test
    void toWebhookRequest_longContentMarksBoundedEvidenceAsTruncated_S2DONE_TC001() {
        ContentReport report = blogReport("x".repeat(4_001));
        CapturingService service = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW,
                AdminReportAiTargetAction.NO_ACTION));

        AdminReportAiResolutionRequestDTO request =
                ReflectionTestUtils.invokeMethod(service, "toWebhookRequest", report);

        AdminReportAiEvidenceItemRequestDTO contentEvidence = request.getEvidence().stream()
                .filter(item -> "EV-TARGET-CONTENT".equals(item.getEvidenceId()))
                .findFirst()
                .orElseThrow();
        assertThat(contentEvidence.getPayload().getTruncated()).isTrue();
        assertThat(contentEvidence.getPayload().getValue().get("sanitizedText").toString())
                .hasSize(4_000);
    }

    @Test
    void toWebhookRequest_inappropriateImageWithoutVisionMarksCriticalEvidenceMissing_S2DONE_TC002() {
        ContentReport report = blogReport("Reported image context");
        report.getReason().setCode("INAPPROPRIATE_IMAGE");
        report.getBlog().setImageUrls(List.of("https://example.com/report-image.png"));
        CapturingService service = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW,
                AdminReportAiTargetAction.NO_ACTION));

        AdminReportAiResolutionRequestDTO request =
                ReflectionTestUtils.invokeMethod(service, "toWebhookRequest", report);

        assertThat(request.getExecutionConstraints())
                .containsEntry("criticalEvidenceMissing", true);
    }

    @Test
    void validateWebhookResponse_defenseInDepthClampsUnexpectedDecisionAction_S2DONE_TC003() {
        AdminReportAiResolutionServiceImpl service = serviceWithRestClient(null);
        AdminReportAiResolutionRequestDTO request = newRequestFor(blogReport("content"));
        AdminReportAiResolutionWebhookResponseDTO response = validResponse(
                request,
                AdminReportAiReportDecision.REJECT,
                AdminReportAiTargetAction.HIDE);

        try (MockedStatic<AdminReportAiSemanticValidator> semanticValidator =
                     mockStatic(AdminReportAiSemanticValidator.class)) {
            semanticValidator.when(() -> AdminReportAiSemanticValidator.validate(request, response))
                    .thenReturn(new AdminReportAiSemanticValidator.ValidationResult(response, false));
            semanticValidator.when(() -> AdminReportAiSemanticValidator.isDecisionActionAllowed(
                            request.getTargetType(),
                            AdminReportAiReportDecision.REJECT,
                            AdminReportAiTargetAction.HIDE))
                    .thenReturn(false);

            ReflectionTestUtils.invokeMethod(service, "validateWebhookResponse", request, response);
        }

        assertThat(response.getRecommendationState()).isEqualTo("NEEDS_MANUAL_REVIEW");
        assertThat(response.getReportDecision())
                .isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        assertThat(response.getTargetAction()).isEqualTo(AdminReportAiTargetAction.NO_ACTION);
        assertThat(response.getBlockedReasons()).containsExactly("DECISION_ACTION_NOT_ALLOWED");
        assertThat(response.getFindings()).isEmpty();
    }

    @Test
    void resolutionRequest_legacyConstructorRetainsPublicCompatibility_S201_TC002() {
        UUID reportId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        AdminReportAiResolutionRequestDTO request = new AdminReportAiResolutionRequestDTO(
                reportId,
                ReportTargetType.BLOG,
                targetId,
                "SPAM",
                "Spam",
                3,
                "Claim",
                "Content",
                List.of("https://example.com/image.png"),
                ReportStatus.OPEN,
                Map.of("decision", "SAFE"),
                2L);

        assertThat(request.getReportId()).isEqualTo(reportId);
        assertThat(request.getTargetType()).isEqualTo(ReportTargetType.BLOG);
        assertThat(request.getTargetId()).isEqualTo(targetId);
        assertThat(request.getReasonCode()).isEqualTo("SPAM");
        assertThat(request.getReasonLabel()).isEqualTo("Spam");
        assertThat(request.getReasonSeverity()).isEqualTo(3);
        assertThat(request.getDescription()).isEqualTo("Claim");
        assertThat(request.getContentText()).isEqualTo("Content");
        assertThat(request.getImageUrls()).containsExactly("https://example.com/image.png");
        assertThat(request.getReportStatus()).isEqualTo(ReportStatus.OPEN);
        assertThat(request.getExistingModerationResult()).containsEntry("decision", "SAFE");
        assertThat(request.getSameTargetOpenReportCount()).isEqualTo(2L);
    }

    @Test
    void createResolution_unknownRuleClampsToManualNoAction_TC002() {
        ContentReport report = blogReport("content");
        stubReport(report);
        CapturingService service = serviceReturning(request -> {
            AdminReportAiResolutionWebhookResponseDTO response = validResponse(
                    request,
                    AdminReportAiReportDecision.RESOLVE,
                    AdminReportAiTargetAction.REMOVE);
            response.getFindings().getFirst().put("ruleId", "MODEL.INVENTED.999");
            return response;
        });

        AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

        assertThat(result.getReportDecision()).isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        assertThat(result.getTargetAction()).isEqualTo(AdminReportAiTargetAction.NO_ACTION);
        assertThat(result.getBlockedReasons()).contains("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
        assertThat(result.getFindings()).isEmpty();
    }

    @Test
    void createResolution_unknownEvidenceReferenceClampsToManual_TC003() {
        ContentReport report = blogReport("content");
        stubReport(report);
        CapturingService service = serviceReturning(request -> {
            AdminReportAiResolutionWebhookResponseDTO response = validResponse(
                    request,
                    AdminReportAiReportDecision.RESOLVE,
                    AdminReportAiTargetAction.HIDE);
            response.getFindings().getFirst().put("evidenceIds", List.of("EV-NOT-REAL"));
            return response;
        });

        AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

        assertThat(result.getReportDecision()).isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        assertThat(result.getBlockedReasons()).contains("UNKNOWN_RULE_OR_EVIDENCE_REFERENCE");
    }

    @Test
    void createResolution_versionMismatchClampsToManual_TC003_1() {
        ContentReport report = blogReport("content");
        stubReport(report);
        CapturingService service = serviceReturning(request -> {
            AdminReportAiResolutionWebhookResponseDTO response = validResponse(
                    request,
                    AdminReportAiReportDecision.RESOLVE,
                    AdminReportAiTargetAction.HIDE);
            response.setRuleCatalogVersion("MODEL-INVENTED-CATALOG");
            return response;
        });

        AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

        assertThat(result.getReportDecision()).isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        assertThat(result.getBlockedReasons()).contains("VERSION_PIN_MISMATCH");
    }

    @Test
    void createResolution_criticalMissingContentClampsProviderConclusion_TC004() {
        ContentReport report = blogReport(" ");
        stubReport(report);
        CapturingService service = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));

        AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

        assertThat(result.getReportDecision()).isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        assertThat(result.getTargetAction()).isEqualTo(AdminReportAiTargetAction.NO_ACTION);
        assertThat(result.getBlockedReasons()).contains("CRITICAL_EVIDENCE_MISSING");
    }

    @Test
    void createResolution_userAndCafePageAreLocalManualOnlyWithoutProviderCall_TC005() {
        for (ContentReport report : List.of(userReport(), cafePageReport())) {
            stubReport(report);
            CapturingService service = serviceReturning(request -> {
                throw new AssertionError("Provider must not be called for USER/CAFE_PAGE in Sprint 1");
            });

            AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

            assertThat(result.getReportDecision()).isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
            assertThat(result.getTargetAction()).isEqualTo(AdminReportAiTargetAction.NO_ACTION);
            assertThat(result.getModelName()).isEqualTo("backend-policy-guard");
            assertThat(result.getBlockedReasons()).contains("TARGET_DEEP_POLICY_NOT_IN_SPRINT1");
            assertThat(service.providerCalls).isZero();
        }
    }

    @Test
    void createResolution_duplicateIdempotencyReturnsExistingRecord_TC006() {
        ContentReport report = blogReport("stable content");
        stubReport(report);
        AdminReportAiResolution existing = existingResolution(report);
        when(resolutionRepository.findByIdempotencyKey(any())).thenReturn(Optional.of(existing));
        CapturingService service = serviceReturning(request -> {
            throw new AssertionError("Duplicate request must not call provider");
        });

        AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

        assertThat(result.getId()).isEqualTo(existing.getId());
        assertThat(service.providerCalls).isZero();
        verify(resolutionRepository, never()).save(any(AdminReportAiResolution.class));
    }

    @Test
    void createResolution_duplicateAutoApplyRequestStillReturnsA0Warning_TC006_1() {
        ContentReport report = blogReport("stable content");
        stubReport(report);
        AdminReportAiResolution existing = existingResolution(report);
        AdminReportAiResolutionCreateRequestDTO createRequest = new AdminReportAiResolutionCreateRequestDTO();
        createRequest.setAutoApplyEnabled(true);
        createRequest.setAutoApplyDelayMinutes(15);
        UUID adminUserId = UUID.randomUUID();
        when(resolutionRepository.findByIdempotencyKey(any())).thenReturn(Optional.of(existing));
        when(autoApplyJobService.scheduleIfRequested(report, existing, createRequest, adminUserId))
                .thenReturn(new AdminReportAiAutoApplyJobService.ScheduleResult(
                        null,
                        "Automation mode A0_RECOMMEND_ONLY is active."));
        CapturingService service = serviceReturning(request -> {
            throw new AssertionError("Duplicate request must not call provider");
        });

        AdminReportAiResolutionResponseDTO result =
                service.createResolution(report.getId(), createRequest, adminUserId);

        assertThat(result.getId()).isEqualTo(existing.getId());
        assertThat(result.getAutoApplyJob()).isNull();
        assertThat(result.getAutoApplyWarning()).contains("A0_RECOMMEND_ONLY");
        assertThat(service.providerCalls).isZero();
        verify(autoApplyJobService).scheduleIfRequested(report, existing, createRequest, adminUserId);
        verify(resolutionRepository, never()).save(any(AdminReportAiResolution.class));
    }

    @Test
    void createResolution_changedSnapshotDoesNotReusePreviouslyMatchedCache_CT010_SAF010() {
        ContentReport report = blogReport("snapshot-v1");
        stubReport(report);
        CapturingService probe = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));
        AdminReportAiResolutionRequestDTO snapshotV1 =
                ReflectionTestUtils.invokeMethod(probe, "toWebhookRequest", report);
        String snapshotV1Hash = String.valueOf(snapshotV1.getTargetSnapshot().get("snapshotHash"));
        AdminReportAiResolution existing = existingResolution(report);
        existing.setIdempotencyKey(snapshotV1.getIdempotencyKey());
        existing.setTargetSnapshotHash(snapshotV1Hash);

        when(resolutionRepository.findByIdempotencyKey(any())).thenAnswer(invocation -> {
            String requestedKey = invocation.getArgument(0);
            if (snapshotV1.getIdempotencyKey().equals(requestedKey)) {
                report.getBlog().setContent("snapshot-v2");
                report.getBlog().setUpdatedAt(LocalDateTime.now());
                return Optional.of(existing);
            }
            return Optional.empty();
        });
        CapturingService service = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));

        AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

        assertThat(result.getId()).isNotEqualTo(existing.getId());
        assertThat(service.providerCalls).isOne();
        assertThat(service.lastRequest.getIdempotencyKey()).isNotEqualTo(snapshotV1.getIdempotencyKey());
        assertThat(service.lastRequest.getTargetSnapshot().get("snapshotHash")).isNotEqualTo(snapshotV1Hash);
        verify(resolutionRepository).save(any(AdminReportAiResolution.class));
    }

    @Test
    void createResolution_targetChangesDuringProviderClampsStaleResult_CT010_1_SAF010() {
        ContentReport report = blogReport("snapshot-before-provider");
        stubReport(report);
        AtomicReference<AdminReportAiResolution> saved = captureSavedResolution();
        CapturingService service = serviceReturning(request -> {
            report.getBlog().setContent("snapshot-after-provider");
            report.getBlog().setUpdatedAt(LocalDateTime.now());
            return validResponse(
                    request,
                    AdminReportAiReportDecision.RESOLVE,
                    AdminReportAiTargetAction.HIDE);
        });

        AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

        assertThat(service.providerCalls).isOne();
        assertThat(result.getReportDecision())
                .isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        assertThat(result.getTargetAction()).isEqualTo(AdminReportAiTargetAction.NO_ACTION);
        assertThat(result.getFindings()).isEmpty();
        assertThat(result.getBlockedReasons()).contains("TARGET_SNAPSHOT_CHANGED_DURING_EVALUATION");
        assertThat(saved.get().getReportDecision())
                .isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.OPEN);
        assertThat(report.getBlog().getStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    void refreshSnapshotSources_supportsNoEntityManagerAndMissingTargetConflict_CT010_2() {
        ContentReport report = blogReport("freshness compatibility");
        stubReport(report);
        AdminReportAiResolutionServiceImpl withoutEntityManager =
                new AdminReportAiResolutionServiceImpl(
                        resolutionRepository,
                        contentReportRepository,
                        moderationResultRepository,
                        autoApplyJobService,
                        objectMapper,
                        webhookSigner,
                        (RestClient) null);

        ReflectionTestUtils.invokeMethod(withoutEntityManager, "refreshSnapshotSources", report);

        doThrow(new EntityNotFoundException("deleted target")).when(entityManager).refresh(report);
        CapturingService service = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));

        assertThatThrownBy(() -> service.createResolution(report.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException responseError = (ResponseStatusException) error;
                    assertThat(responseError.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(responseError.getReason()).contains("target changed");
                });
        verify(resolutionRepository, never()).save(any(AdminReportAiResolution.class));
    }

    @Test
    void createResolution_autoApplyRequestReturnsBlockedOutcomeAndNoJob_TC007() {
        ContentReport report = blogReport("content");
        stubReport(report);
        AdminReportAiResolutionCreateRequestDTO createRequest = new AdminReportAiResolutionCreateRequestDTO();
        createRequest.setAutoApplyEnabled(true);
        createRequest.setAutoApplyDelayMinutes(15);
        when(autoApplyJobService.scheduleIfRequested(eq(report), any(), eq(createRequest), any()))
                .thenReturn(new AdminReportAiAutoApplyJobService.ScheduleResult(
                        null,
                        "Automation mode A0_RECOMMEND_ONLY is active."));
        CapturingService service = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.REJECT,
                AdminReportAiTargetAction.KEEP_VISIBLE));

        AdminReportAiResolutionResponseDTO result =
                service.createResolution(report.getId(), createRequest, UUID.randomUUID());

        assertThat(result.getAutoApplyJob()).isNull();
        assertThat(result.getAutoApplyWarning()).contains("A0_RECOMMEND_ONLY");
    }

    @Test
    void createResolution_terminalReportFailsBeforeProvider_TC008() {
        ContentReport report = blogReport("content");
        report.setStatus(ReportStatus.RESOLVED);
        stubReport(report);
        CapturingService service = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));

        assertThatThrownBy(() -> service.createResolution(report.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
        assertThat(service.providerCalls).isZero();
    }

    @Test
    void createResolution_missingRequiredProviderMetadataFailsWithoutSave_TC009() {
        ContentReport report = blogReport("content");
        stubReport(report);
        CapturingService service = serviceReturning(request -> new AdminReportAiResolutionWebhookResponseDTO());

        assertThatThrownBy(() -> service.createResolution(report.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
        verify(resolutionRepository, never()).save(any(AdminReportAiResolution.class));
    }

    @Test
    void getResolutions_legacyRowIsMarkedLegacyAndRawResponseIsNotExposed_TC010() {
        ContentReport report = blogReport("content");
        stubReport(report);
        AdminReportAiResolution legacy = existingResolution(report);
        legacy.setContractVersion(null);
        legacy.setRawResponse(Map.of("providerSecret", "must-not-leak"));
        PageRequest pageable = PageRequest.of(0, 20);
        when(resolutionRepository.findByContentReportId(report.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(legacy)));

        AdminReportAiResolutionResponseDTO result =
                serviceReturning(request -> null).getResolutions(report.getId(), pageable).getContent().getFirst();
        JsonNode json = objectMapper.valueToTree(result);

        assertThat(result.getContractVersion()).isEqualTo("legacy-v1");
        assertThat(json.has("rawResponse")).isFalse();
        assertThat(json.toString()).doesNotContain("providerSecret");
    }

    @Test
    void callWebhook_validJsonParsesV2Response_TC011() throws Exception {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AdminReportAiResolutionRequestDTO request = new AdminReportAiResolutionRequestDTO();
        request.setReportId(UUID.randomUUID());
        request.setCorrelationId(UUID.randomUUID());
        AdminReportAiResolutionWebhookResponseDTO expected = validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE);
        AdminReportAiWebhookSigner.SignedRequest signedResponse =
                webhookSigner.signRequest(expected, "2.0", request.getCorrelationId());
        server.expect(requestTo("http://localhost"))
                .andExpect(header(AdminReportAiWebhookSigner.CONTRACT_VERSION_HEADER, "2.0"))
                .andExpect(header(
                        AdminReportAiWebhookSigner.CORRELATION_ID_HEADER,
                        request.getCorrelationId().toString()))
                .andExpect(clientRequest -> {
                    assertThat(clientRequest.getHeaders().getFirst(AdminReportAiWebhookSigner.TIMESTAMP_HEADER))
                            .matches("\\d+");
                    assertThat(clientRequest.getHeaders().getFirst(AdminReportAiWebhookSigner.NONCE_HEADER))
                            .matches("[0-9a-f-]{36}");
                    assertThat(clientRequest.getHeaders().getFirst(AdminReportAiWebhookSigner.BODY_SHA256_HEADER))
                            .matches("[0-9a-f]{64}");
                    assertThat(clientRequest.getHeaders().getFirst(AdminReportAiWebhookSigner.SIGNATURE_HEADER))
                            .matches("[0-9a-f]{64}");
                })
                .andRespond(withSuccess(signedResponse.body(), MediaType.APPLICATION_JSON)
                        .header(
                                AdminReportAiWebhookSigner.CONTRACT_VERSION_HEADER,
                                signedResponse.contractVersion())
                        .header(
                                AdminReportAiWebhookSigner.CORRELATION_ID_HEADER,
                                signedResponse.correlationId())
                        .header(AdminReportAiWebhookSigner.TIMESTAMP_HEADER, signedResponse.timestamp())
                        .header(AdminReportAiWebhookSigner.NONCE_HEADER, signedResponse.nonce())
                        .header(AdminReportAiWebhookSigner.BODY_SHA256_HEADER, signedResponse.bodyHash())
                        .header(AdminReportAiWebhookSigner.SIGNATURE_HEADER, signedResponse.signature()));

        AdminReportAiResolutionWebhookResponseDTO actual = new AdminReportAiResolutionServiceImpl(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                autoApplyJobService,
                objectMapper,
                webhookSigner,
                builder.build()).callWebhook(request);

        assertThat(actual.getContractVersion()).isEqualTo("2.0");
        assertThat(actual.getCorrelationId()).isEqualTo(request.getCorrelationId());
        server.verify();
    }

    @Test
    void callWebhook_unsignedResponseFailsClosed_TC012() throws Exception {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AdminReportAiResolutionRequestDTO request = new AdminReportAiResolutionRequestDTO();
        request.setReportId(UUID.randomUUID());
        request.setCorrelationId(UUID.randomUUID());
        AdminReportAiResolutionWebhookResponseDTO response = validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE);
        server.expect(requestTo("http://localhost"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));
        AdminReportAiResolutionServiceImpl service = new AdminReportAiResolutionServiceImpl(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                autoApplyJobService,
                objectMapper,
                webhookSigner,
                builder.build());

        assertThatThrownBy(() -> service.callWebhook(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException responseError = (ResponseStatusException) error;
                    assertThat(responseError.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(responseError.getReason()).contains("failed security verification");
                });
        server.verify();
    }

    @Test
    void callWebhook_non2xxEmptyAndSignedNonObjectJsonFailClosed_TC013() throws Exception {
        AdminReportAiResolutionRequestDTO request = webhookRequest();

        RestClient.Builder non2xxBuilder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer non2xxServer = MockRestServiceServer.bindTo(non2xxBuilder).build();
        non2xxServer.expect(requestTo("http://localhost"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        assertThatThrownBy(() -> serviceWithRestClient(non2xxBuilder.build()).callWebhook(request))
                .isInstanceOf(AdminReportAiProviderBoundaryException.class)
                .satisfies(error -> {
                    AdminReportAiProviderBoundaryException providerError =
                            (AdminReportAiProviderBoundaryException) error;
                    assertThat(providerError.getMessage())
                            .isEqualTo(AdminReportAiProviderBoundaryException.SAFE_MESSAGE);
                    assertThat(providerError.getCorrelationId()).isEqualTo(request.getCorrelationId());
                    assertThat(providerError.getCause()).isInstanceOf(IllegalStateException.class);
                });
        non2xxServer.verify();

        RestClient.Builder emptyBuilder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer emptyServer = MockRestServiceServer.bindTo(emptyBuilder).build();
        emptyServer.expect(requestTo("http://localhost"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> serviceWithRestClient(emptyBuilder.build()).callWebhook(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .contains("response is empty"));
        emptyServer.verify();

        RestClient.Builder invalidJsonBuilder = RestClient.builder().baseUrl("http://localhost");
        MockRestServiceServer invalidJsonServer = MockRestServiceServer.bindTo(invalidJsonBuilder).build();
        AdminReportAiWebhookSigner.SignedRequest signedArray =
                webhookSigner.signRequest("[]", "2.0", request.getCorrelationId());
        invalidJsonServer.expect(requestTo("http://localhost"))
                .andRespond(withStatus(HttpStatus.OK)
                        .body(signedArray.body())
                        .header(AdminReportAiWebhookSigner.CONTRACT_VERSION_HEADER, signedArray.contractVersion())
                        .header(AdminReportAiWebhookSigner.CORRELATION_ID_HEADER, signedArray.correlationId())
                        .header(AdminReportAiWebhookSigner.TIMESTAMP_HEADER, signedArray.timestamp())
                        .header(AdminReportAiWebhookSigner.NONCE_HEADER, signedArray.nonce())
                        .header(AdminReportAiWebhookSigner.BODY_SHA256_HEADER, signedArray.bodyHash())
                        .header(AdminReportAiWebhookSigner.SIGNATURE_HEADER, signedArray.signature()));
        assertThatThrownBy(() -> serviceWithRestClient(invalidJsonBuilder.build()).callWebhook(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .contains("not valid JSON"));
        invalidJsonServer.verify();
    }

    @Test
    void constructorAndReportLookupValidationCoverConfiguredAndMissingInputs_TC014() {
        AdminReportAiResolutionServiceImpl configured = new AdminReportAiResolutionServiceImpl(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                autoApplyJobService,
                objectMapper,
                webhookSigner,
                "http://localhost:5678/webhook/cafestory-admin-report-ai-resolution",
                1_000);
        assertThat(configured).isNotNull();

        CapturingService service = serviceReturning(request -> null);
        assertThatThrownBy(() -> service.createResolution(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        UUID missingId = UUID.randomUUID();
        when(contentReportRepository.findById(missingId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createResolution(missingId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createResolution_commentContextsMediaAndModerationAreCaptured_TC015() {
        ContentReport withParent = commentReport(true);
        withParent.getComment().setImageUrls(List.of("https://example.com/comment.png"));
        stubReport(withParent);
        AiModerationResult moderation = new AiModerationResult();
        moderation.setId(UUID.randomUUID());
        moderation.setDecision(ModerationDecision.VIOLATION);
        moderation.setScore(91.0);
        moderation.setTags(List.of("spam"));
        moderation.setExplanation("Derived moderation signal");
        moderation.setAiStatus("COMPLETED");
        moderation.setPriorityScore(80.0);
        moderation.setRiskScore(70.0);
        moderation.setResolved(false);
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(withParent.getId()))
                .thenReturn(Optional.of(moderation));
        when(moderationResultRepository.findTopByCommentIdOrderByCreatedAtDesc(withParent.getComment().getId()))
                .thenReturn(Optional.of(moderation));
        CapturingService parentService = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.REMOVE));

        AdminReportAiResolutionResponseDTO parentResult = parentService.createResolution(withParent.getId());

        assertThat(parentResult.getActionRisk()).isEqualTo("CRITICAL");
        assertThat(parentService.lastRequest.getTargetSnapshot().get("observableFields").toString())
                .contains("parentBlogId", "parentBlogExcerpt");
        assertThat(parentService.lastRequest.getEvidence())
                .extracting(AdminReportAiEvidenceItemRequestDTO::getEvidenceId)
                .contains(
                        "EV-PARENT-CONTEXT",
                        "EV-COMMENT-THREAD",
                        "EV-TARGET-MEDIA",
                        "EV-TARGET-AUTHOR",
                        "EV-TARGET-REPORT-HISTORY",
                        "EV-TARGET-MODERATION-HISTORY")
                .doesNotContain("EV-DERIVED-MODERATION");
        AdminReportAiEvidenceItemRequestDTO mediaEvidence = parentService.lastRequest.getEvidence().stream()
                .filter(item -> "EV-TARGET-MEDIA".equals(item.getEvidenceId()))
                .findFirst()
                .orElseThrow();
        assertThat(mediaEvidence.getAvailability().getStatus()).isEqualTo("AVAILABLE");
        assertThat(mediaEvidence.getQuality().getLevel()).isEqualTo("MEDIUM");
        assertThat(mediaEvidence.getPayload().getValue())
                .containsEntry("verificationMethod", "PLATFORM_URL_METADATA_ONLY")
                .containsEntry("visionScanned", false);
        assertThat(parentService.lastRequest.getExistingModerationResult())
                .containsKeys("id", "decision", "score", "labels", "explanation", "aiStatus",
                        "priorityScore", "riskScore", "resolved");

        ContentReport withoutParent = commentReport(false);
        stubReport(withoutParent);
        CapturingService missingParentService = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));

        AdminReportAiResolutionResponseDTO missingParentResult =
                missingParentService.createResolution(withoutParent.getId());

        assertThat(missingParentResult.getReportDecision())
                .isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        assertThat(missingParentService.lastRequest.getExecutionConstraints())
                .containsEntry("criticalEvidenceMissing", true);
        AdminReportAiEvidenceItemRequestDTO parentEvidence =
                missingParentService.lastRequest.getEvidence().stream()
                .filter(item -> "EV-PARENT-CONTEXT".equals(item.getEvidenceId()))
                .findFirst()
                .orElseThrow();
        assertThat(parentEvidence.getAvailability().getStatus()).isEqualTo("MISSING");
        assertThat(parentEvidence.getPayload().getRepresentation()).isEqualTo("NONE");

        ContentReport blankParent = commentReport(true);
        blankParent.getComment().getBlog().setContent("   ");
        stubReport(blankParent);
        CapturingService blankParentService = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE));

        AdminReportAiResolutionResponseDTO blankParentResult =
                blankParentService.createResolution(blankParent.getId());

        assertThat(blankParentResult.getReportDecision())
                .isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        assertThat(blankParentResult.getTargetAction())
                .isEqualTo(AdminReportAiTargetAction.NO_ACTION);
        assertThat(blankParentResult.getBlockedReasons())
                .contains("CRITICAL_EVIDENCE_MISSING");
        assertThat(blankParentResult.getEvidenceSufficiency())
                .isEqualTo("UNASSESSABLE");
        assertThat(blankParentService.lastRequest.getExecutionConstraints())
                .containsEntry("criticalEvidenceMissing", true);
        AdminReportAiEvidenceItemRequestDTO blankParentEvidence =
                blankParentService.lastRequest.getEvidence().stream()
                .filter(item -> "EV-PARENT-CONTEXT".equals(item.getEvidenceId()))
                .findFirst()
                .orElseThrow();
        assertThat(blankParentEvidence.getQuality().getLevel()).isEqualTo("UNUSABLE");
        assertThat(blankParentEvidence.getAvailability().getStatus()).isEqualTo("MISSING");
        assertThat(blankParentEvidence.getPayload().getValue()).isNull();
    }

    @Test
    void createResolution_nullReasonOptionalCollectionsAndInvalidActionAreSafe_TC016() {
        ContentReport report = blogReport("content");
        report.setReason(null);
        report.getBlog().setImageUrls(null);
        stubReport(report);
        CapturingService service = serviceReturning(request -> {
            AdminReportAiResolutionWebhookResponseDTO response = validResponse(
                    request,
                    AdminReportAiReportDecision.REJECT,
                    AdminReportAiTargetAction.HIDE);
            response.getFindings().getFirst().put("ruleId", "CSR.ROUTE.002");
            return response;
        });

        AdminReportAiResolutionResponseDTO result = service.createResolution(report.getId());

        assertThat(service.lastRequest.getReasonCode()).isNull();
        assertThat(service.lastRequest.getReasonSeverity()).isNull();
        assertThat(service.lastRequest.getImageUrls()).isEmpty();
        assertThat(result.getReportDecision()).isEqualTo(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        assertThat(result.getBlockedReasons()).contains("DECISION_ACTION_NOT_ALLOWED");

        AdminReportAiResolutionRequestDTO webhookRequest =
                ReflectionTestUtils.invokeMethod(service, "toWebhookRequest", report);
        AdminReportAiResolutionWebhookResponseDTO sparse = validResponse(
                webhookRequest,
                AdminReportAiReportDecision.REJECT,
                AdminReportAiTargetAction.KEEP_VISIBLE);
        sparse.setLabels(null);
        sparse.setFindings(null);
        sparse.setEvidenceSummary(null);
        sparse.setBlockedReasons(null);
        AdminReportAiResolution sparseEntity =
                ReflectionTestUtils.invokeMethod(service, "toEntity", report, webhookRequest, sparse);
        assertThat(sparseEntity.getLabels()).isEmpty();
        assertThat(sparseEntity.getFindings()).isEmpty();
        assertThat(sparseEntity.getEvidenceSummary()).isEmpty();
        assertThat(sparseEntity.getBlockedReasons()).isEmpty();
        assertThat(sparseEntity.getActionRisk()).isEqualTo("LOW");
    }

    @Test
    void createResolution_missingAuditMetadataAndMissingTargetsFailClosed_TC017() {
        ContentReport report = blogReport("content");
        stubReport(report);
        AdminReportAiResolutionWebhookResponseDTO blankExplanation = validResponse(
                newRequestFor(report),
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE);
        blankExplanation.setExplanation(" ");
        assertThatThrownBy(() -> serviceReturning(request -> {
            blankExplanation.setCorrelationId(request.getCorrelationId());
            return blankExplanation;
        }).createResolution(report.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .contains("audit metadata"));

        AdminReportAiResolutionWebhookResponseDTO blankModel = validResponse(
                newRequestFor(report),
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE);
        blankModel.setModelName(" ");
        assertThatThrownBy(() -> serviceReturning(request -> {
            blankModel.setCorrelationId(request.getCorrelationId());
            return blankModel;
        }).createResolution(report.getId()))
                .isInstanceOf(ResponseStatusException.class);

        for (ContentReport missingTarget : List.of(
                baseReport(ReportTargetType.BLOG),
                baseReport(ReportTargetType.COMMENT),
                baseReport(ReportTargetType.USER),
                baseReport(ReportTargetType.CAFE_PAGE))) {
            stubReport(missingTarget);
            assertThatThrownBy(() -> serviceReturning(request -> null)
                    .createResolution(missingTarget.getId()))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                            .contains("target is unavailable"));
        }
    }

    @Test
    void getResolutions_legacyNullFieldsUseSafeCompatibilityDefaults_TC018() {
        ContentReport report = blogReport("content");
        stubReport(report);
        AdminReportAiResolution legacy = new AdminReportAiResolution();
        legacy.setId(UUID.randomUUID());
        legacy.setContentReport(null);
        legacy.setContractVersion(null);
        legacy.setAutomationMode(null);
        legacy.setReportDecision(null);
        PageRequest pageable = PageRequest.of(0, 20);
        when(resolutionRepository.findByContentReportId(report.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(legacy)));

        AdminReportAiResolutionResponseDTO result =
                serviceReturning(request -> null).getResolutions(report.getId(), pageable)
                        .getContent().getFirst();

        assertThat(result.getContractVersion()).isEqualTo("legacy-v1");
        assertThat(result.getAutomationMode()).isEqualTo("A0_RECOMMEND_ONLY");
        assertThat(result.getRecommendationState()).isNull();
        assertThat(result.getContentReportId()).isNull();
    }

    @Test
    void privateFailureAdaptersConvertSerializationAndDigestFailures_TC019() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any()))
                .thenThrow(JsonMappingException.fromUnexpectedIOE(new java.io.IOException("boom")));
        AdminReportAiResolutionServiceImpl serializationService = new AdminReportAiResolutionServiceImpl(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                autoApplyJobService,
                failingMapper,
                webhookSigner,
                (RestClient) null);

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(
                serializationService,
                "toCanonicalJson",
                Map.of("value", "x")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unable to serialize");

        AdminReportAiResolutionServiceImpl service = serviceWithRestClient(null);
        try (MockedStatic<MessageDigest> digest = mockStatic(MessageDigest.class)) {
            digest.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("missing"));
            assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(service, "sha256", "value"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("SHA-256 is not available");
        }

        assertThat((String) ReflectionTestUtils.invokeMethod(service, "truncate", "abcdef", 3))
                .isEqualTo("abc");
        assertThat((String) ReflectionTestUtils.invokeMethod(service, "blankToNull", " "))
                .isNull();
    }

    @Test
    void createResolution_manualTargetsIncludeOnlyNonBlankMediaReferences_TC020() {
        ContentReport userReport = userReport();
        userReport.getReportedUser().setUserAvatar("https://example.com/user.png");
        stubReport(userReport);
        CapturingService userService = serviceReturning(request -> {
            throw new AssertionError("Manual-only USER must not call provider");
        });

        userService.createResolution(userReport.getId());

        assertThat(userService.lastRequest).isNull();
        List<String> userImages = ReflectionTestUtils.invokeMethod(userService, "imageUrls", userReport);
        assertThat(userImages)
                .containsExactly("https://example.com/user.png");

        ContentReport pageReport = cafePageReport();
        pageReport.getCafePage().setAvatarUrl("https://example.com/page-avatar.png");
        pageReport.getCafePage().setCoverUrl(" ");
        stubReport(pageReport);
        CapturingService pageService = serviceReturning(request -> {
            throw new AssertionError("Manual-only CAFE_PAGE must not call provider");
        });

        pageService.createResolution(pageReport.getId());

        assertThat(pageService.lastRequest).isNull();
        List<String> pageImages = ReflectionTestUtils.invokeMethod(pageService, "imageUrls", pageReport);
        assertThat(pageImages)
                .containsExactly("https://example.com/page-avatar.png");
    }

    private AdminReportAiResolutionServiceImpl serviceWithRestClient(RestClient restClient) {
        return new AdminReportAiResolutionServiceImpl(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                autoApplyJobService,
                objectMapper,
                webhookSigner,
                restClient);
    }

    private AdminReportAiResolutionRequestDTO webhookRequest() {
        AdminReportAiResolutionRequestDTO request = new AdminReportAiResolutionRequestDTO();
        request.setReportId(UUID.randomUUID());
        request.setCorrelationId(UUID.randomUUID());
        return request;
    }

    private AdminReportAiResolutionRequestDTO newRequestFor(ContentReport report) {
        AdminReportAiResolutionRequestDTO request = new AdminReportAiResolutionRequestDTO();
        request.setReportId(report.getId());
        request.setCorrelationId(UUID.randomUUID());
        request.setTargetType(report.getTargetType());
        request.setReasonCode(report.getReason() == null ? null : report.getReason().getCode());
        request.setTargetSnapshot(Map.of(
                "snapshotHash", "sha256:test-snapshot",
                "snapshotVersion", "1"));
        request.setEvidence(List.of(evidenceItem("EV-TARGET-CONTENT", report.getTargetType(), "sha256:test-snapshot")));
        request.setPolicyContext(activePolicyContext(request.getReasonCode()));
        request.setExecutionConstraints(Map.of("criticalEvidenceMissing", false));
        return request;
    }

    private AdminReportAiPolicyContextRequestDTO activePolicyContext(String reasonCode) {
        AdminReportAiPolicyContextRequestDTO context = AdminReportAiPolicyContextRequestDTO.builder()
                .contextSchemaVersion(AdminReportAiPolicyCatalog.CONTEXT_SCHEMA_VERSION)
                .policyVersion(AdminReportAiPolicyCatalog.POLICY_VERSION)
                .policyStatus("ACTIVE")
                .ruleCatalogVersion(AdminReportAiPolicyCatalog.RULE_CATALOG_VERSION)
                .ruleCatalogStatus("ACTIVE")
                .requirementMatrixVersion(AdminReportAiPolicyCatalog.REQUIREMENT_MATRIX_VERSION)
                .evidenceKindCatalogVersion(AdminReportAiPolicyCatalog.EVIDENCE_KIND_CATALOG_VERSION)
                .evaluationMode("ACTIVE_RUNTIME")
                .candidateRules(AdminReportAiPolicyCatalog.candidateRules(reasonCode))
                .availableEvidenceKinds(List.of("TARGET_TEXT_CONTENT"))
                .missingRequirements(List.of())
                .currentEvaluationCeiling("RESOLVE_OR_REJECT")
                .build();
        context.getCandidateRules().forEach(rule -> {
            rule.setRuleStatus("ACTIVE");
            rule.setEvaluationCeiling("RESOLVE_OR_REJECT");
        });
        return context;
    }

    private AdminReportAiEvidenceItemRequestDTO evidenceItem(
            String evidenceId,
            ReportTargetType targetType,
            String snapshotHash) {
        return AdminReportAiEvidenceItemRequestDTO.builder()
                .evidenceId(evidenceId)
                .envelopeVersion(AdminReportAiPolicyCatalog.EVIDENCE_ENVELOPE_VERSION)
                .evidenceKind("TARGET_TEXT_CONTENT")
                .subject(AdminReportAiEvidenceItemRequestDTO.Subject.builder()
                        .targetType(targetType)
                        .targetAlias("target-test-1")
                        .snapshotVersion("1")
                        .snapshotHash(snapshotHash)
                        .build())
                .source(AdminReportAiEvidenceItemRequestDTO.Source.builder()
                        .sourceType("TARGET_SNAPSHOT")
                        .sourceSystem("CAFE_STORY_BACKEND")
                        .verificationStatus("SYSTEM_CAPTURED")
                        .authorityScope("PLATFORM_OWNED_FIELD")
                        .build())
                .capture(AdminReportAiEvidenceItemRequestDTO.Capture.builder()
                        .collectorName("test")
                        .collectorVersion("1")
                        .transformations(List.of())
                        .build())
                .integrity(AdminReportAiEvidenceItemRequestDTO.Integrity.builder()
                        .canonicalization("JCS")
                        .digestAlgorithm("SHA-256")
                        .payloadDigest("sha256:test-payload")
                        .build())
                .availability(AdminReportAiEvidenceItemRequestDTO.Availability.builder()
                        .status("AVAILABLE")
                        .build())
                .quality(AdminReportAiEvidenceItemRequestDTO.Quality.builder()
                        .level("HIGH")
                        .reasonCodes(List.of("TEST"))
                        .build())
                .privacy(AdminReportAiEvidenceItemRequestDTO.Privacy.builder()
                        .classification("PUBLIC_CONTENT")
                        .containsPersonalData(false)
                        .redactionStatus("NOT_REQUIRED")
                        .retentionClass("REPORT_EVIDENCE_90D")
                        .build())
                .intendedUse("RULE_EVALUATION_CANDIDATE")
                .collectedForRuleIds(List.of("CSR.INT.003"))
                .payload(AdminReportAiEvidenceItemRequestDTO.Payload.builder()
                        .representation("INLINE")
                        .mediaType("application/json")
                        .value(Map.of("sanitizedText", "test"))
                        .truncated(false)
                        .build())
                .build();
    }

    private AtomicReference<AdminReportAiResolution> captureSavedResolution() {
        AtomicReference<AdminReportAiResolution> captured = new AtomicReference<>();
        when(resolutionRepository.save(any(AdminReportAiResolution.class))).thenAnswer(invocation -> {
            AdminReportAiResolution resolution = invocation.getArgument(0);
            resolution.setId(UUID.randomUUID());
            resolution.setCreatedAt(LocalDateTime.now());
            captured.set(resolution);
            return resolution;
        });
        return captured;
    }

    private void stubReport(ContentReport report) {
        when(contentReportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(report.getId()))
                .thenReturn(Optional.empty());
        when(contentReportRepository.countByBlogIdAndStatusIn(any(), any())).thenReturn(1L);
        when(contentReportRepository.countByCommentIdAndStatusIn(any(), any())).thenReturn(1L);
        when(contentReportRepository.countByReportedUserUserIdAndStatusIn(any(), any())).thenReturn(1L);
        when(contentReportRepository.countByCafePageIdAndStatusIn(any(), any())).thenReturn(1L);
    }

    private CapturingService serviceReturning(
            Function<AdminReportAiResolutionRequestDTO, AdminReportAiResolutionWebhookResponseDTO> responder) {
        return new CapturingService(responder);
    }

    private AdminReportAiResolutionWebhookResponseDTO validResponse(
            AdminReportAiResolutionRequestDTO request,
            AdminReportAiReportDecision decision,
            AdminReportAiTargetAction action) {
        List<com.cafestory.dto.requestDTO.AdminReportAiCandidateRuleRequestDTO> candidates =
                request.getPolicyContext() == null
                        ? AdminReportAiPolicyCatalog.candidateRules("SPAM")
                        : request.getPolicyContext().getCandidateRules();
        List<com.cafestory.dto.requestDTO.AdminReportAiCandidateRuleRequestDTO> materialRules =
                candidates.stream()
                        .filter(rule -> Boolean.TRUE.equals(rule.getMaterial()))
                        .toList();
        if (materialRules.isEmpty()) {
            materialRules = AdminReportAiPolicyCatalog.candidateRules("SPAM");
        }
        List<Map<String, Object>> findings = new java.util.ArrayList<>();
        for (int index = 0; index < materialRules.size(); index++) {
            var rule = materialRules.get(index);
            String outcome = decision == AdminReportAiReportDecision.REJECT
                    ? "NOT_SUBSTANTIATED"
                    : index == 0 ? "SUBSTANTIATED" : "NOT_SUBSTANTIATED";
            findings.add(new java.util.LinkedHashMap<>(Map.of(
                    "ruleId", rule.getRuleId(),
                    "ruleVersion", rule.getRuleVersion(),
                    "outcome", outcome,
                    "evidenceIds", "SUBSTANTIATED".equals(outcome)
                            ? List.of("EV-TARGET-CONTENT")
                            : List.of(),
                    "counterEvidenceIds", List.of(),
                    "missingEvidenceIds", List.of(),
                    "violationLikelihood", "SUBSTANTIATED".equals(outcome) ? "HIGH" : "LOW",
                    "rationale", "Derived explanation, not evidence.")));
        }
        AdminReportAiResolutionWebhookResponseDTO response = new AdminReportAiResolutionWebhookResponseDTO();
        response.setContractVersion("2.0");
        response.setCorrelationId(request.getCorrelationId());
        response.setRecommendationState(decision.name());
        response.setReportDecision(decision);
        response.setTargetAction(action);
        response.setLabels(List.of("policy-review"));
        response.setExplanation("AI rationale based on referenced platform evidence; this is not evidence itself.");
        response.setModelName("gpt-4o-mini");
        response.setFindings(findings);
        response.setEvidenceSummary(Map.of(
                "usedEvidenceIds", List.of("EV-TARGET-CONTENT"),
                "counterEvidenceIds", List.of(),
                "missingEvidenceIds", List.of()));
        response.setBlockedReasons(List.of());
        response.setEvidenceQuality("HIGH");
        response.setEvidenceSufficiency("SUFFICIENT");
        response.setViolationLikelihood(decision == AdminReportAiReportDecision.REJECT ? "LOW" : "HIGH");
        response.setHarmSeverity("MEDIUM");
        response.setPolicyVersion(AdminReportAiPolicyCatalog.POLICY_VERSION);
        response.setRuleCatalogVersion(AdminReportAiPolicyCatalog.RULE_CATALOG_VERSION);
        response.setPromptVersion(AdminReportAiPolicyCatalog.PROMPT_VERSION);
        response.setWorkflowVersion(AdminReportAiPolicyCatalog.WORKFLOW_VERSION);
        return response;
    }

    private ContentReport blogReport(String content) {
        ContentReport report = baseReport(ReportTargetType.BLOG);
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setContent(content);
        blog.setStatus(PostStatus.PUBLISHED);
        blog.setImageUrls(List.of());
        blog.setCreatedAt(LocalDateTime.now().minusDays(1));
        report.setBlog(blog);
        return report;
    }

    private ContentReport userReport() {
        ContentReport report = baseReport(ReportTargetType.USER);
        User user = user("reported");
        user.setUserDescription("Profile text");
        user.setAccountStatus(true);
        report.setReportedUser(user);
        return report;
    }

    private ContentReport cafePageReport() {
        ContentReport report = baseReport(ReportTargetType.CAFE_PAGE);
        CafePage page = new CafePage();
        page.setId(UUID.randomUUID());
        page.setName("Cafe Test");
        page.setDescription("Cafe description");
        page.setStatus(PageStatus.ACTIVE);
        page.setCreatedAt(LocalDateTime.now().minusDays(2));
        report.setCafePage(page);
        return report;
    }

    private ContentReport commentReport(boolean withParentBlog) {
        ContentReport report = baseReport(ReportTargetType.COMMENT);
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setContent("Reported comment content");
        comment.setStatus(PostStatus.PUBLISHED);
        comment.setImageUrls(List.of());
        comment.setCreatedAt(LocalDateTime.now().minusHours(3));
        comment.setUpdatedAt(LocalDateTime.now().minusHours(1));
        if (withParentBlog) {
            Blog parent = new Blog();
            parent.setId(UUID.randomUUID());
            parent.setContent("x".repeat(1_100));
            parent.setStatus(PostStatus.PUBLISHED);
            parent.setImageUrls(List.of());
            parent.setCreatedAt(LocalDateTime.now().minusDays(2));
            parent.setUpdatedAt(LocalDateTime.now().minusDays(1));
            comment.setBlog(parent);
        }
        report.setComment(comment);
        return report;
    }

    private ContentReport baseReport(ReportTargetType targetType) {
        ReportReason reason = new ReportReason();
        reason.setId(UUID.randomUUID());
        reason.setCode("SCAM_FRAUD_OR_SPAM");
        reason.setLabelVi("Spam hoặc lừa đảo");
        reason.setSeverity(4);

        ContentReport report = new ContentReport();
        report.setId(UUID.randomUUID());
        report.setReporter(user("reporter"));
        report.setTargetType(targetType);
        report.setReason(reason);
        report.setReasonSnapshot(reason.getLabelVi());
        report.setDescription("Reporter claim");
        report.setStatus(ReportStatus.OPEN);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    private User user(String username) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName(username);
        user.setUserEmail(username + "@example.com");
        user.setUserPassword("password");
        return user;
    }

    private AdminReportAiResolution existingResolution(ContentReport report) {
        AdminReportAiResolution resolution = new AdminReportAiResolution();
        resolution.setId(UUID.randomUUID());
        resolution.setContentReport(report);
        resolution.setContractVersion("2.0");
        resolution.setCorrelationId(UUID.randomUUID());
        resolution.setAutomationMode("A0_RECOMMEND_ONLY");
        resolution.setTargetType(report.getTargetType());
        resolution.setTargetId(report.getTargetType() == ReportTargetType.BLOG
                ? report.getBlog().getId()
                : UUID.randomUUID());
        resolution.setReportDecision(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        resolution.setTargetAction(AdminReportAiTargetAction.NO_ACTION);
        resolution.setModelName("backend-policy-guard");
        resolution.setCreatedAt(LocalDateTime.now());
        return resolution;
    }

    private class CapturingService extends AdminReportAiResolutionServiceImpl {
        private final Function<AdminReportAiResolutionRequestDTO, AdminReportAiResolutionWebhookResponseDTO> responder;
        private AdminReportAiResolutionRequestDTO lastRequest;
        private int providerCalls;

        CapturingService(
                Function<AdminReportAiResolutionRequestDTO, AdminReportAiResolutionWebhookResponseDTO> responder) {
            super(
                    resolutionRepository,
                    contentReportRepository,
                    moderationResultRepository,
                    autoApplyJobService,
                    objectMapper,
                    webhookSigner,
                    (RestClient) null,
                    entityManager);
            this.responder = responder;
        }

        @Override
        protected AdminReportAiResolutionWebhookResponseDTO callWebhook(AdminReportAiResolutionRequestDTO request) {
            providerCalls++;
            lastRequest = request;
            request.getPolicyContext().setPolicyStatus("ACTIVE");
            request.getPolicyContext().setRuleCatalogStatus("ACTIVE");
            request.getPolicyContext().setEvaluationMode("ACTIVE_RUNTIME");
            request.getPolicyContext().getCandidateRules()
                    .forEach(rule -> {
                        rule.setRuleStatus("ACTIVE");
                        rule.setEvaluationCeiling("RESOLVE_OR_REJECT");
                    });
            request.getPolicyContext().setCurrentEvaluationCeiling("RESOLVE_OR_REJECT");
            return responder.apply(request);
        }
    }
}
