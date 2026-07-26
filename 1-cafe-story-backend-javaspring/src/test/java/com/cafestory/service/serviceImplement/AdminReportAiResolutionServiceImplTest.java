package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
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
import com.cafestory.repository.AdminReportAiResolutionRepository;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @BeforeEach
    void setUp() {
        resolutionRepository = mock(AdminReportAiResolutionRepository.class);
        contentReportRepository = mock(ContentReportRepository.class);
        moderationResultRepository = mock(AiModerationResultRepository.class);
        autoApplyJobService = mock(AdminReportAiAutoApplyJobService.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();
        webhookSigner = new AdminReportAiWebhookSigner(objectMapper, "unit-test-secret", 120, 300, Clock.systemUTC());
        when(resolutionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(resolutionRepository.save(any(AdminReportAiResolution.class))).thenAnswer(invocation -> {
            AdminReportAiResolution resolution = invocation.getArgument(0);
            resolution.setId(UUID.randomUUID());
            resolution.setCreatedAt(LocalDateTime.now());
            return resolution;
        });
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
        assertThat(result.getFindings()).hasSize(1);
        assertThat(result.getEvidenceSufficiency()).isEqualTo("SUFFICIENT");
        assertThat(result.getTargetSnapshotHash()).hasSize(64);
        assertThat(saved.get().getRawResponse()).isNull();
        assertThat(saved.get().getConfidenceScore()).isNull();
        assertThat(saved.get().getRiskScore()).isNull();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.OPEN);
        assertThat(report.getBlog().getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(service.lastRequest.getPolicyContext()).containsKeys(
                "policyVersion", "ruleCatalogVersion", "candidateRules");
        assertThat(service.lastRequest.getEvidence())
                .extracting(item -> item.get("evidenceId"))
                .contains("EV-TARGET-IDENTITY", "EV-TARGET-CONTENT", "EV-TARGET-STATE", "EV-REASON-ROUTE");
        verify(contentReportRepository, never()).save(any(ContentReport.class));
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
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
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
        CapturingService parentService = serviceReturning(request -> validResponse(
                request,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.REMOVE));

        AdminReportAiResolutionResponseDTO parentResult = parentService.createResolution(withParent.getId());

        assertThat(parentResult.getActionRisk()).isEqualTo("CRITICAL");
        assertThat(parentService.lastRequest.getTargetSnapshot().get("observableFields").toString())
                .contains("parentBlogId", "parentBlogExcerpt");
        assertThat(parentService.lastRequest.getEvidence())
                .extracting(item -> item.get("evidenceId"))
                .contains("EV-PARENT-CONTEXT", "EV-TARGET-MEDIA", "EV-DERIVED-MODERATION");
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
        Map<String, Object> parentEvidence = missingParentService.lastRequest.getEvidence().stream()
                .filter(item -> "EV-PARENT-CONTEXT".equals(item.get("evidenceId")))
                .findFirst()
                .orElseThrow();
        assertThat(parentEvidence).containsEntry("availability", "MISSING");
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
        request.setEvidence(List.of(Map.of("evidenceId", "EV-TARGET-CONTENT")));
        request.setExecutionConstraints(Map.of("criticalEvidenceMissing", false));
        return request;
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
        AdminReportAiResolutionWebhookResponseDTO response = new AdminReportAiResolutionWebhookResponseDTO();
        response.setContractVersion("2.0");
        response.setCorrelationId(request.getCorrelationId());
        response.setRecommendationState(decision.name());
        response.setReportDecision(decision);
        response.setTargetAction(action);
        response.setLabels(List.of("policy-review"));
        response.setExplanation("AI rationale based on referenced platform evidence; this is not evidence itself.");
        response.setModelName("gpt-4o-mini");
        response.setFindings(List.of(new java.util.LinkedHashMap<>(Map.of(
                "ruleId", "CSR.INT.003",
                "ruleVersion", "1.0.0-proposed.1",
                "outcome", decision == AdminReportAiReportDecision.REJECT ? "NOT_SUPPORTED" : "SUPPORTED",
                "evidenceIds", List.of("EV-TARGET-CONTENT"),
                "counterEvidenceIds", List.of(),
                "missingEvidenceIds", List.of(),
                "violationLikelihood", decision == AdminReportAiReportDecision.REJECT ? "LOW" : "HIGH",
                "rationale", "Derived explanation, not evidence."))));
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
                    (RestClient) null);
            this.responder = responder;
        }

        @Override
        protected AdminReportAiResolutionWebhookResponseDTO callWebhook(AdminReportAiResolutionRequestDTO request) {
            providerCalls++;
            lastRequest = request;
            return responder.apply(request);
        }
    }
}
