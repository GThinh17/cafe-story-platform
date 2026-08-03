package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiCandidateRuleRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiEvidenceItemRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiMissingRequirementRequestDTO;
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
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.exception.AdminReportAiProviderBoundaryException;
import com.cafestory.repository.AdminReportAiResolutionRepository;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import com.cafestory.service.serviceInterface.AdminReportAiResolutionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminReportAiResolutionServiceImpl implements AdminReportAiResolutionService {

    private static final Logger log = LoggerFactory.getLogger(AdminReportAiResolutionServiceImpl.class);
    private static final List<ReportStatus> ACTIVE_REPORT_STATUSES =
            List.of(ReportStatus.OPEN, ReportStatus.REVIEWING);
    private static final String CONTRACT_VERSION = "2.0";
    private static final String AUTOMATION_MODE = "A0_RECOMMEND_ONLY";

    private final AdminReportAiResolutionRepository resolutionRepository;
    private final ContentReportRepository contentReportRepository;
    private final AiModerationResultRepository moderationResultRepository;
    private final AdminReportAiAutoApplyJobService autoApplyJobService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final AdminReportAiWebhookSigner webhookSigner;
    private final EntityManager entityManager;

    @Autowired
    public AdminReportAiResolutionServiceImpl(
            AdminReportAiResolutionRepository resolutionRepository,
            ContentReportRepository contentReportRepository,
            AiModerationResultRepository moderationResultRepository,
            AdminReportAiAutoApplyJobService autoApplyJobService,
            ObjectMapper objectMapper,
            AdminReportAiWebhookSigner webhookSigner,
            EntityManager entityManager,
            @Value("${admin.report.ai.webhook-url:http://localhost:5678/webhook/cafestory-admin-report-ai-resolution}")
            String webhookUrl,
            @Value("${admin.report.ai.timeout-ms:40000}") int timeoutMs) {
        this(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                autoApplyJobService,
                objectMapper,
                webhookSigner,
                RestClient.builder()
                        .baseUrl(webhookUrl)
                        .requestFactory(requestFactory(timeoutMs))
                        .build(),
                entityManager);
    }

    public AdminReportAiResolutionServiceImpl(
            AdminReportAiResolutionRepository resolutionRepository,
            ContentReportRepository contentReportRepository,
            AiModerationResultRepository moderationResultRepository,
            AdminReportAiAutoApplyJobService autoApplyJobService,
            ObjectMapper objectMapper,
            AdminReportAiWebhookSigner webhookSigner,
            String webhookUrl,
            int timeoutMs) {
        this(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                autoApplyJobService,
                objectMapper,
                webhookSigner,
                null,
                webhookUrl,
                timeoutMs);
    }

    AdminReportAiResolutionServiceImpl(
            AdminReportAiResolutionRepository resolutionRepository,
            ContentReportRepository contentReportRepository,
            AiModerationResultRepository moderationResultRepository,
            AdminReportAiAutoApplyJobService autoApplyJobService,
            ObjectMapper objectMapper,
            AdminReportAiWebhookSigner webhookSigner,
            RestClient restClient) {
        this(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                autoApplyJobService,
                objectMapper,
                webhookSigner,
                restClient,
                null);
    }

    AdminReportAiResolutionServiceImpl(
            AdminReportAiResolutionRepository resolutionRepository,
            ContentReportRepository contentReportRepository,
            AiModerationResultRepository moderationResultRepository,
            AdminReportAiAutoApplyJobService autoApplyJobService,
            ObjectMapper objectMapper,
            AdminReportAiWebhookSigner webhookSigner,
            RestClient restClient,
            EntityManager entityManager) {
        this.resolutionRepository = resolutionRepository;
        this.contentReportRepository = contentReportRepository;
        this.moderationResultRepository = moderationResultRepository;
        this.autoApplyJobService = autoApplyJobService;
        this.objectMapper = objectMapper;
        this.webhookSigner = webhookSigner;
        this.restClient = restClient;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public AdminReportAiResolutionResponseDTO createResolution(UUID reportId) {
        return createResolution(reportId, null, null);
    }

    @Override
    @Transactional
    public AdminReportAiResolutionResponseDTO createResolution(
            UUID reportId,
            AdminReportAiResolutionCreateRequestDTO request,
            UUID adminUserId) {
        ContentReport report = findReport(reportId);
        validateReportCanRequestAi(report);
        AdminReportAiResolutionRequestDTO webhookRequest = toWebhookRequest(report);
        Optional<AdminReportAiResolution> existing =
                resolutionRepository.findByIdempotencyKey(webhookRequest.getIdempotencyKey());
        if (existing.isPresent()) {
            AdminReportAiResolutionRequestDTO refreshedRequest = refreshAndBuildRequest(report);
            if (Objects.equals(
                    webhookRequest.getIdempotencyKey(),
                    refreshedRequest.getIdempotencyKey())) {
                return withAutoApplyOutcome(
                        report,
                        existing.get(),
                        toResponse(existing.get()),
                        request,
                        adminUserId);
            }
            webhookRequest = refreshedRequest;
        }

        AdminReportAiResolutionWebhookResponseDTO webhookResponse =
                isManualOnlyTarget(report.getTargetType())
                        ? localManualOnlyResponse(webhookRequest, "TARGET_DEEP_POLICY_NOT_IN_SPRINT1")
                        : callWebhook(webhookRequest);
        AdminReportAiResolutionRequestDTO currentRequest = refreshAndBuildRequest(report);
        if (!Objects.equals(
                webhookRequest.getIdempotencyKey(),
                currentRequest.getIdempotencyKey())) {
            webhookResponse = localManualOnlyResponse(
                    webhookRequest,
                    "TARGET_SNAPSHOT_CHANGED_DURING_EVALUATION");
        }
        validateWebhookResponse(webhookRequest, webhookResponse);
        AdminReportAiResolution savedResolution =
                resolutionRepository.save(toEntity(report, webhookRequest, webhookResponse));
        return withAutoApplyOutcome(
                report,
                savedResolution,
                toResponse(savedResolution),
                request,
                adminUserId);
    }

    private AdminReportAiResolutionResponseDTO withAutoApplyOutcome(
            ContentReport report,
            AdminReportAiResolution resolution,
            AdminReportAiResolutionResponseDTO response,
            AdminReportAiResolutionCreateRequestDTO request,
            UUID adminUserId) {
        if (autoApplyJobService != null && request != null && request.isAutoApplyEnabled()) {
            AdminReportAiAutoApplyJobService.ScheduleResult scheduleResult =
                    autoApplyJobService.scheduleIfRequested(report, resolution, request, adminUserId);
            response.setAutoApplyJob(scheduleResult.job());
            response.setAutoApplyWarning(scheduleResult.warning());
        }
        return response;
    }

    private AdminReportAiResolutionRequestDTO refreshAndBuildRequest(ContentReport report) {
        refreshSnapshotSources(report);
        validateReportCanRequestAi(report);
        return toWebhookRequest(report);
    }

    private void refreshSnapshotSources(ContentReport report) {
        if (entityManager == null) {
            return;
        }
        try {
            entityManager.refresh(report);
            switch (report.getTargetType()) {
                case BLOG -> refreshIfPresent(report.getBlog());
                case COMMENT -> {
                    Comment comment = report.getComment();
                    refreshIfPresent(comment);
                    if (comment != null) {
                        refreshIfPresent(comment.getBlog());
                    }
                }
                case USER -> refreshIfPresent(report.getReportedUser());
                case CAFE_PAGE -> refreshIfPresent(report.getCafePage());
            }
        } catch (EntityNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Report target changed during AI evaluation");
        }
    }

    private void refreshIfPresent(Object entity) {
        if (entity != null) {
            entityManager.refresh(entity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportAiResolutionResponseDTO> getResolutions(UUID reportId, Pageable pageable) {
        ContentReport report = findReport(reportId);
        return resolutionRepository.findByContentReportId(report.getId(), pageable)
                .map(this::toResponse);
    }

    protected AdminReportAiResolutionWebhookResponseDTO callWebhook(AdminReportAiResolutionRequestDTO request) {
        RawWebhookResponse rawResponse;
        try {
            AdminReportAiWebhookSigner.SignedRequest signedRequest =
                    webhookSigner.signRequest(request, CONTRACT_VERSION, request.getCorrelationId());
            rawResponse = restClient.post()
                    .uri("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(signedRequest::apply)
                    .body(signedRequest.body())
                    .exchange((clientRequest, clientResponse) -> {
                        byte[] responseBytes = clientResponse.getBody().readAllBytes();
                        if (!clientResponse.getStatusCode().is2xxSuccessful()) {
                            throw new IllegalStateException("Admin report AI webhook returned "
                                    + clientResponse.getStatusCode());
                        }
                        MediaType contentType = clientResponse.getHeaders().getContentType();
                        HttpHeaders responseHeaders = new HttpHeaders();
                        responseHeaders.putAll(clientResponse.getHeaders());
                        return new RawWebhookResponse(
                                new String(responseBytes, StandardCharsets.UTF_8),
                                contentType == null ? "unknown" : contentType.toString(),
                                responseHeaders);
                    });
        } catch (RuntimeException exception) {
            log.warn("Admin report AI webhook failed reportId={} reason={}", request.getReportId(), exception.getMessage());
            throw new AdminReportAiProviderBoundaryException(request.getCorrelationId(), exception);
        }

        String responseBody = rawResponse.body();
        if (responseBody == null || responseBody.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Admin report AI resolution response is empty");
        }

        try {
            webhookSigner.verifyResponse(
                    responseBody,
                    rawResponse.headers(),
                    CONTRACT_VERSION,
                    request.getCorrelationId());
        } catch (RuntimeException exception) {
            log.warn(
                    "Admin report AI response security verification failed reportId={} correlationId={} reason={}",
                    request.getReportId(),
                    request.getCorrelationId(),
                    exception.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Admin report AI resolution response failed security verification");
        }

        try {
            return objectMapper.readValue(responseBody, AdminReportAiResolutionWebhookResponseDTO.class);
        } catch (IOException exception) {
            log.warn(
                    "Admin report AI response parse failed reportId={} correlationId={} contentType={}",
                    request.getReportId(),
                    request.getCorrelationId(),
                    rawResponse.contentType(),
                    exception);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Admin report AI resolution response is not valid JSON");
        }
    }

    private ContentReport findReport(UUID reportId) {
        if (reportId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report id is required");
        }
        return contentReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
    }

    private AdminReportAiResolutionRequestDTO toWebhookRequest(ContentReport report) {
        AdminReportAiResolutionRequestDTO request = new AdminReportAiResolutionRequestDTO();
        request.setContractVersion(CONTRACT_VERSION);
        request.setCorrelationId(UUID.randomUUID());
        request.setRequestedAt(OffsetDateTime.now(ZoneOffset.UTC));
        request.setAutomationMode(AUTOMATION_MODE);
        request.setReportId(report.getId());
        request.setTargetType(report.getTargetType());
        request.setTargetId(targetId(report));
        request.setReasonCode(report.getReason() == null ? null : report.getReason().getCode());
        request.setReasonLabel(report.getReasonSnapshot());
        request.setReasonSeverity(report.getReason() == null ? null : report.getReason().getSeverity());
        request.setDescription(report.getDescription());
        request.setContentText(contentText(report));
        request.setImageUrls(imageUrls(report));
        request.setReportStatus(report.getStatus());
        request.setExistingModerationResult(existingModerationResult(report.getId()));
        request.setSameTargetOpenReportCount(sameTargetOpenReportCount(report));

        request.setReportClaim(reportClaim(request));
        request.setTargetSnapshot(targetSnapshot(report));
        AdminReportAiPolicyContextRequestDTO policyContext = policyContext(request.getReasonCode());
        request.setEvidence(evidence(report, request, policyContext));
        completePolicyContext(policyContext, request.getEvidence());
        request.setPolicyContext(policyContext);
        request.setExecutionConstraints(executionConstraints(report, request));
        String snapshotHash = String.valueOf(request.getTargetSnapshot().get("snapshotHash"));
        request.setIdempotencyKey(sha256(String.join("|",
                report.getId().toString(),
                report.getTargetType().name(),
                request.getTargetId().toString(),
                snapshotHash,
                AdminReportAiPolicyCatalog.POLICY_VERSION,
                AdminReportAiPolicyCatalog.RULE_CATALOG_VERSION,
                AdminReportAiPolicyCatalog.PROMPT_VERSION,
                AdminReportAiPolicyCatalog.WORKFLOW_VERSION)));
        return request;
    }

    private AdminReportAiResolution toEntity(
            ContentReport report,
            AdminReportAiResolutionRequestDTO webhookRequest,
            AdminReportAiResolutionWebhookResponseDTO webhookResponse) {
        AdminReportAiResolution resolution = new AdminReportAiResolution();
        resolution.setContentReport(report);
        resolution.setContractVersion(CONTRACT_VERSION);
        resolution.setCorrelationId(webhookResponse.getCorrelationId());
        resolution.setIdempotencyKey(webhookRequest.getIdempotencyKey());
        resolution.setAutomationMode(AUTOMATION_MODE);
        resolution.setTargetType(report.getTargetType());
        resolution.setTargetId(targetId(report));
        resolution.setReportDecision(webhookResponse.getReportDecision());
        resolution.setTargetAction(webhookResponse.getTargetAction());
        resolution.setConfidenceScore(null);
        resolution.setRiskScore(null);
        resolution.setLabels(webhookResponse.getLabels() == null ? List.of() : webhookResponse.getLabels());
        resolution.setRuleCode(null);
        resolution.setExplanation(blankToNull(webhookResponse.getExplanation()));
        resolution.setModelName(webhookResponse.getModelName().trim());
        resolution.setRawResponse(null);
        resolution.setPolicyVersion(AdminReportAiPolicyCatalog.POLICY_VERSION);
        resolution.setRuleCatalogVersion(AdminReportAiPolicyCatalog.RULE_CATALOG_VERSION);
        resolution.setPromptVersion(AdminReportAiPolicyCatalog.PROMPT_VERSION);
        resolution.setWorkflowVersion(AdminReportAiPolicyCatalog.WORKFLOW_VERSION);
        resolution.setTargetSnapshotHash(String.valueOf(webhookRequest.getTargetSnapshot().get("snapshotHash")));
        resolution.setEvidenceQuality(webhookResponse.getEvidenceQuality());
        resolution.setEvidenceSufficiency(webhookResponse.getEvidenceSufficiency());
        resolution.setViolationLikelihood(webhookResponse.getViolationLikelihood());
        resolution.setHarmSeverity(webhookResponse.getHarmSeverity());
        resolution.setActionRisk(actionRisk(report.getTargetType(), webhookResponse.getTargetAction()));
        resolution.setFindings(webhookResponse.getFindings() == null ? List.of() : webhookResponse.getFindings());
        resolution.setEvidenceSummary(webhookResponse.getEvidenceSummary() == null
                ? Map.of()
                : webhookResponse.getEvidenceSummary());
        resolution.setBlockedReasons(webhookResponse.getBlockedReasons() == null
                ? List.of()
                : webhookResponse.getBlockedReasons());
        return resolution;
    }

    private Map<String, Object> reportClaim(AdminReportAiResolutionRequestDTO request) {
        Map<String, Object> claim = new LinkedHashMap<>();
        claim.put("reportId", request.getReportId());
        claim.put("status", request.getReportStatus());
        claim.put("reasonCode", request.getReasonCode());
        claim.put("reasonCatalogVersion", AdminReportAiPolicyCatalog.REASON_CATALOG_VERSION);
        claim.put("description", request.getDescription());
        claim.put("trustLevel", "UNTRUSTED_REPORTER_CLAIM");
        return claim;
    }

    private Map<String, Object> targetSnapshot(ContentReport report) {
        Map<String, Object> observable = new LinkedHashMap<>();
        observable.put("contentText", contentText(report));
        observable.put("imageUrls", imageUrls(report));
        observable.put("status", targetStatus(report));
        observable.put("createdAt", isoTimestamp(targetCreatedAt(report)));
        observable.put("updatedAt", isoTimestamp(targetUpdatedAt(report)));
        if (report.getTargetType() == ReportTargetType.COMMENT && report.getComment().getBlog() != null) {
            Blog parentBlog = report.getComment().getBlog();
            observable.put("parentBlogId", parentBlog.getId());
            observable.put(
                    "parentBlogExcerpt",
                    hasUsableParentContext(parentBlog) ? truncate(parentBlog.getContent(), 500) : null);
        }

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("targetType", report.getTargetType());
        snapshot.put("targetId", targetId(report));
        snapshot.put("targetAlias", targetAlias(report));
        snapshot.put("snapshotVersion", "1");
        snapshot.put("capturedAt", OffsetDateTime.now(ZoneOffset.UTC));
        snapshot.put("observableFields", observable);
        snapshot.put("snapshotHash", digest(toCanonicalJson(observable)));
        return snapshot;
    }

    private List<AdminReportAiEvidenceItemRequestDTO> evidence(
            ContentReport report,
            AdminReportAiResolutionRequestDTO request,
            AdminReportAiPolicyContextRequestDTO policyContext) {
        List<String> candidateRuleIds = policyContext.getCandidateRules().stream()
                .map(AdminReportAiCandidateRuleRequestDTO::getRuleId)
                .toList();
        List<AdminReportAiEvidenceItemRequestDTO> evidence = new ArrayList<>();
        evidence.add(evidenceItem(
                "EV-TARGET-IDENTITY",
                "TARGET_IDENTITY",
                request,
                report,
                "TARGET_SNAPSHOT",
                "target",
                Map.of(
                        "targetType", report.getTargetType().name(),
                        "targetAlias", targetAlias(report)),
                false,
                null,
                "HIGH",
                "DIRECT_PLATFORM_RECORD",
                "AVAILABLE",
                null,
                "CONTEXT_ONLY",
                candidateRuleIds));
        boolean contentAvailable = request.getContentText() != null && !request.getContentText().isBlank();
        evidence.add(evidenceItem(
                "EV-TARGET-CONTENT",
                "TARGET_TEXT_CONTENT",
                request,
                report,
                "TARGET_SNAPSHOT",
                "observableFields.contentText",
                contentAvailable
                        ? Map.of("sanitizedText", truncate(request.getContentText(), 4000))
                        : null,
                contentAvailable && request.getContentText().length() > 4000,
                "SANITIZE_AND_BOUND_TEXT",
                contentAvailable ? "HIGH" : "UNUSABLE",
                contentAvailable ? "AUTHORITATIVE_PLATFORM_FIELD" : "SOURCE_FIELD_EMPTY",
                contentAvailable ? "AVAILABLE" : "MISSING",
                contentAvailable ? null : "TARGET_TEXT_NOT_AVAILABLE",
                "RULE_EVALUATION_CANDIDATE",
                candidateRuleIds));
        Map<String, Object> statePayload = new LinkedHashMap<>();
        statePayload.put("status", targetStatus(report));
        statePayload.put("createdAt", isoTimestamp(targetCreatedAt(report)));
        statePayload.put("updatedAt", isoTimestamp(targetUpdatedAt(report)));
        evidence.add(evidenceItem(
                "EV-TARGET-STATE",
                "TARGET_STATE",
                request,
                report,
                "TARGET_SNAPSHOT",
                "observableFields.status",
                statePayload,
                false,
                null,
                "HIGH",
                "FRESH_AT_CAPTURE",
                "AVAILABLE",
                null,
                "CONTEXT_ONLY",
                candidateRuleIds));
        if (report.getTargetType() == ReportTargetType.COMMENT) {
            Blog parentBlog = report.getComment().getBlog();
            boolean parentContextAvailable = hasUsableParentContext(parentBlog);
            evidence.add(evidenceItem(
                    "EV-PARENT-CONTEXT",
                    "PARENT_BLOG_CONTEXT",
                    request,
                    report,
                    "PLATFORM_RECORD",
                    "comment.blog.content",
                    parentContextAvailable
                            ? Map.of("sanitizedExcerpt", truncate(parentBlog.getContent(), 1000))
                            : null,
                    parentContextAvailable && parentBlog.getContent().length() > 1000,
                    "SANITIZE_AND_BOUND_CONTEXT",
                    parentContextAvailable ? "HIGH" : "UNUSABLE",
                    parentContextAvailable ? "BOUNDED_CONTEXT" : "SOURCE_CONTEXT_EMPTY",
                    parentContextAvailable ? "AVAILABLE" : "MISSING",
                    parentContextAvailable ? null : "PARENT_CONTEXT_UNAVAILABLE_OR_INVALID",
                    "CONTEXT_ONLY",
                    candidateRuleIds));
        }
        if (!request.getImageUrls().isEmpty()) {
            evidence.add(evidenceItem(
                    "EV-TARGET-MEDIA",
                    "TARGET_MEDIA_REFERENCE",
                    request,
                    report,
                    "TARGET_SNAPSHOT",
                    "observableFields.imageUrls",
                    Map.of("referenceCount", request.getImageUrls().size()),
                    false,
                    null,
                    "UNUSABLE",
                    "MEDIA_NOT_FETCHED_OR_VERIFIED",
                    "NOT_COLLECTED",
                    "VERIFIED_MEDIA_OBSERVATION_NOT_AVAILABLE",
                    "CONTEXT_ONLY",
                    candidateRuleIds));
        }
        return evidence;
    }

    private AdminReportAiPolicyContextRequestDTO policyContext(String reasonCode) {
        return AdminReportAiPolicyContextRequestDTO.builder()
                .contextSchemaVersion(AdminReportAiPolicyCatalog.CONTEXT_SCHEMA_VERSION)
                .policyVersion(AdminReportAiPolicyCatalog.POLICY_VERSION)
                .policyStatus(AdminReportAiPolicyCatalog.POLICY_STATUS)
                .ruleCatalogVersion(AdminReportAiPolicyCatalog.RULE_CATALOG_VERSION)
                .ruleCatalogStatus(AdminReportAiPolicyCatalog.RULE_CATALOG_STATUS)
                .requirementMatrixVersion(AdminReportAiPolicyCatalog.REQUIREMENT_MATRIX_VERSION)
                .evidenceKindCatalogVersion(AdminReportAiPolicyCatalog.EVIDENCE_KIND_CATALOG_VERSION)
                .evaluationMode(AdminReportAiPolicyCatalog.EVALUATION_MODE)
                .candidateRules(AdminReportAiPolicyCatalog.candidateRules(reasonCode))
                .availableEvidenceKinds(List.of())
                .missingRequirements(List.of())
                .currentEvaluationCeiling(AdminReportAiPolicyCatalog.EVALUATION_CEILING)
                .build();
    }

    private void completePolicyContext(
            AdminReportAiPolicyContextRequestDTO policyContext,
            List<AdminReportAiEvidenceItemRequestDTO> evidence) {
        LinkedHashSet<String> availableKinds = new LinkedHashSet<>();
        for (AdminReportAiEvidenceItemRequestDTO item : evidence) {
            if ("AVAILABLE".equals(item.getAvailability().getStatus())
                    && !"UNUSABLE".equals(item.getQuality().getLevel())) {
                availableKinds.add(item.getEvidenceKind());
            }
        }

        List<AdminReportAiMissingRequirementRequestDTO> missing = new ArrayList<>();
        for (AdminReportAiCandidateRuleRequestDTO rule : policyContext.getCandidateRules()) {
            for (String evidenceKind : rule.getRequiredEvidenceKinds()) {
                if (!availableKinds.contains(evidenceKind)) {
                    missing.add(AdminReportAiMissingRequirementRequestDTO.builder()
                            .missingRequirementId("ME-" + rule.getRuleId().replace('.', '-') + "-" + evidenceKind)
                            .ruleId(rule.getRuleId())
                            .requirementCode("REQ-" + evidenceKind)
                            .evidenceKind(evidenceKind)
                            .status("MISSING")
                            .reasonCode("REQUIRED_EVIDENCE_NOT_AVAILABLE")
                            .build());
                }
            }
        }
        policyContext.setAvailableEvidenceKinds(List.copyOf(availableKinds));
        policyContext.setMissingRequirements(List.copyOf(missing));
    }

    private Map<String, Object> executionConstraints(
            ContentReport report,
            AdminReportAiResolutionRequestDTO request) {
        boolean missingContent = request.getContentText() == null || request.getContentText().isBlank();
        boolean mediaUnevaluated = request.getImageUrls() != null
                && !request.getImageUrls().isEmpty()
                && "INAPPROPRIATE_IMAGE".equalsIgnoreCase(request.getReasonCode());
        boolean parentMissing = report.getTargetType() == ReportTargetType.COMMENT
                && !hasUsableParentContext(report.getComment().getBlog());
        Map<String, Object> constraints = new LinkedHashMap<>();
        constraints.put("recommendationOnly", true);
        constraints.put("allowedCandidateActions", List.of("NO_ACTION", "KEEP_VISIBLE", "HIDE", "REMOVE"));
        constraints.put("criticalMissingBehavior", "NEEDS_MANUAL_REVIEW");
        constraints.put("criticalEvidenceMissing", missingContent || mediaUnevaluated || parentMissing);
        return constraints;
    }

    private boolean hasUsableParentContext(Blog parentBlog) {
        return parentBlog != null
                && parentBlog.getContent() != null
                && !parentBlog.getContent().isBlank();
    }

    private AdminReportAiEvidenceItemRequestDTO evidenceItem(
            String evidenceId,
            String evidenceKind,
            AdminReportAiResolutionRequestDTO request,
            ContentReport report,
            String sourceType,
            String sourceFieldPath,
            Map<String, Object> payloadValue,
            boolean truncated,
            String transformationType,
            String quality,
            String qualityReason,
            String availability,
            String availabilityReason,
            String intendedUse,
            List<String> collectedForRuleIds) {
        String snapshotHash = String.valueOf(request.getTargetSnapshot().get("snapshotHash"));
        String snapshotVersion = String.valueOf(request.getTargetSnapshot().get("snapshotVersion"));
        List<AdminReportAiEvidenceItemRequestDTO.Transformation> transformations =
                transformationType == null
                        ? List.of()
                        : List.of(AdminReportAiEvidenceItemRequestDTO.Transformation.builder()
                        .type(transformationType)
                        .version("1.0.0")
                        .materiality("NON_MATERIAL")
                        .build());
        AdminReportAiEvidenceItemRequestDTO.Payload payload =
                AdminReportAiEvidenceItemRequestDTO.Payload.builder()
                        .representation(payloadValue == null ? "NONE" : "INLINE")
                        .mediaType(payloadValue == null ? null : "application/json")
                        .value(payloadValue)
                        .reference(null)
                        .truncated(truncated)
                        .build();
        return AdminReportAiEvidenceItemRequestDTO.builder()
                .evidenceId(evidenceId)
                .envelopeVersion(AdminReportAiPolicyCatalog.EVIDENCE_ENVELOPE_VERSION)
                .evidenceKind(evidenceKind)
                .subject(AdminReportAiEvidenceItemRequestDTO.Subject.builder()
                        .targetType(report.getTargetType())
                        .targetAlias(targetAlias(report))
                        .snapshotVersion(snapshotVersion)
                        .snapshotHash(snapshotHash)
                        .build())
                .source(AdminReportAiEvidenceItemRequestDTO.Source.builder()
                        .sourceType(sourceType)
                        .sourceSystem("CAFE_STORY_BACKEND")
                        .sourceEntityType(report.getTargetType().name())
                        .sourceEntityAlias(targetAlias(report))
                        .sourceFieldPath(sourceFieldPath)
                        .verificationStatus("SYSTEM_CAPTURED")
                        .authorityScope("PLATFORM_OWNED_FIELD")
                        .build())
                .capture(AdminReportAiEvidenceItemRequestDTO.Capture.builder()
                        .collectorName("AdminReportAiResolutionService")
                        .collectorVersion("2.0.0")
                        .capturedAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .sourceUpdatedAt(null)
                        .transformations(transformations)
                        .build())
                .integrity(AdminReportAiEvidenceItemRequestDTO.Integrity.builder()
                        .canonicalization("JCS")
                        .digestAlgorithm("SHA-256")
                        .payloadDigest(payloadValue == null ? null : digest(toCanonicalJson(payloadValue)))
                        .sourceDigest(snapshotHash)
                        .build())
                .availability(AdminReportAiEvidenceItemRequestDTO.Availability.builder()
                        .status(availability)
                        .reasonCode(availabilityReason)
                        .build())
                .quality(AdminReportAiEvidenceItemRequestDTO.Quality.builder()
                        .level(quality)
                        .reasonCodes(List.of(qualityReason))
                        .build())
                .privacy(AdminReportAiEvidenceItemRequestDTO.Privacy.builder()
                        .classification("TARGET_TEXT_CONTENT".equals(evidenceKind)
                                ? "PUBLIC_CONTENT"
                                : "INTERNAL_MODERATION")
                        .containsPersonalData("TARGET_TEXT_CONTENT".equals(evidenceKind))
                        .redactionStatus("APPLIED")
                        .retentionClass("REPORT_EVIDENCE_90D")
                        .build())
                .intendedUse(intendedUse)
                .collectedForRuleIds(List.copyOf(collectedForRuleIds))
                .payload(payload)
                .build();
    }

    private String targetAlias(ContentReport report) {
        return "target-" + report.getTargetType().name().toLowerCase()
                + "-" + sha256(targetId(report).toString()).substring(0, 12);
    }

    private String digest(String value) {
        return "sha256:" + sha256(value);
    }

    private AdminReportAiResolutionWebhookResponseDTO localManualOnlyResponse(
            AdminReportAiResolutionRequestDTO request,
            String blockedReason) {
        AdminReportAiResolutionWebhookResponseDTO response = new AdminReportAiResolutionWebhookResponseDTO();
        response.setContractVersion(CONTRACT_VERSION);
        response.setCorrelationId(request.getCorrelationId());
        response.setRecommendationState("NEEDS_MANUAL_REVIEW");
        response.setReportDecision(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
        response.setTargetAction(AdminReportAiTargetAction.NO_ACTION);
        response.setLabels(List.of("manual-review"));
        response.setExplanation("Target-deep policy evaluation is outside Sprint 1; an admin must review this report.");
        response.setModelName("backend-policy-guard");
        response.setFindings(List.of());
        response.setEvidenceSummary(Map.of(
                "usedEvidenceIds", List.of("EV-TARGET-IDENTITY", "EV-TARGET-STATE"),
                "missingEvidenceIds", List.of("EV-TARGET-DEEP-REVIEW")));
        response.setBlockedReasons(List.of(blockedReason));
        response.setEvidenceQuality("LOW");
        response.setEvidenceSufficiency("UNASSESSABLE");
        response.setViolationLikelihood("UNKNOWN");
        response.setHarmSeverity("UNKNOWN");
        response.setPolicyVersion(AdminReportAiPolicyCatalog.POLICY_VERSION);
        response.setRuleCatalogVersion(AdminReportAiPolicyCatalog.RULE_CATALOG_VERSION);
        response.setPromptVersion(AdminReportAiPolicyCatalog.PROMPT_VERSION);
        response.setWorkflowVersion(AdminReportAiPolicyCatalog.WORKFLOW_VERSION);
        return response;
    }

    private boolean isManualOnlyTarget(ReportTargetType targetType) {
        return targetType == ReportTargetType.USER || targetType == ReportTargetType.CAFE_PAGE;
    }

    private String actionRisk(ReportTargetType targetType, AdminReportAiTargetAction action) {
        if (action == AdminReportAiTargetAction.REMOVE
                || action == AdminReportAiTargetAction.SUSPEND_USER
                || action == AdminReportAiTargetAction.SUSPEND_PAGE) {
            return "CRITICAL";
        }
        if (action == AdminReportAiTargetAction.HIDE) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private Object targetStatus(ContentReport report) {
        return switch (report.getTargetType()) {
            case BLOG -> report.getBlog().getStatus();
            case COMMENT -> report.getComment().getStatus();
            case USER -> report.getReportedUser().getAccountStatus();
            case CAFE_PAGE -> report.getCafePage().getStatus();
        };
    }

    private LocalDateTime targetCreatedAt(ContentReport report) {
        return switch (report.getTargetType()) {
            case BLOG -> report.getBlog().getCreatedAt();
            case COMMENT -> report.getComment().getCreatedAt();
            case USER -> null;
            case CAFE_PAGE -> report.getCafePage().getCreatedAt();
        };
    }

    private LocalDateTime targetUpdatedAt(ContentReport report) {
        return switch (report.getTargetType()) {
            case BLOG -> report.getBlog().getUpdatedAt();
            case COMMENT -> report.getComment().getUpdatedAt();
            case USER -> null;
            case CAFE_PAGE -> report.getCafePage().getUpdatedAt();
        };
    }

    private String toCanonicalJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to serialize Admin Report AI target snapshot", exception);
        }
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String isoTimestamp(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    private void validateWebhookResponse(
            AdminReportAiResolutionRequestDTO request,
            AdminReportAiResolutionWebhookResponseDTO response) {
        if (response == null
                || response.getReportDecision() == null
                || response.getTargetAction() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Admin report AI resolution response is missing required fields");
        }
        if (response.getExplanation() == null || response.getExplanation().isBlank()
                || response.getModelName() == null || response.getModelName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Admin report AI resolution response is missing audit metadata");
        }
        AdminReportAiSemanticValidator.ValidationResult validation =
                AdminReportAiSemanticValidator.validate(request, response);
        AdminReportAiResolutionWebhookResponseDTO validated = validation.response();
        if (!AdminReportAiSemanticValidator.isDecisionActionAllowed(
                request.getTargetType(),
                validated.getReportDecision(),
                validated.getTargetAction())) {
            validated.setRecommendationState("NEEDS_MANUAL_REVIEW");
            validated.setReportDecision(AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW);
            validated.setTargetAction(AdminReportAiTargetAction.NO_ACTION);
            List<String> reasons = new ArrayList<>(validated.getBlockedReasons());
            reasons.add("DECISION_ACTION_NOT_ALLOWED");
            validated.setBlockedReasons(reasons.stream().distinct().toList());
            validated.setFindings(List.of());
        }
    }

    private void validateReportCanRequestAi(ContentReport report) {
        if (!ACTIVE_REPORT_STATUSES.contains(report.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "AI resolution can only be requested for OPEN or REVIEWING reports");
        }
        if (report.getTargetType() == null || targetId(report) == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Report target is unavailable for AI resolution");
        }
    }

    private Map<String, Object> existingModerationResult(UUID reportId) {
        return moderationResultRepository.findTopByContentReportIdOrderByCreatedAtDesc(reportId)
                .map(result -> {
                    Map<String, Object> value = new LinkedHashMap<>();
                    value.put("id", result.getId());
                    value.put("decision", result.getDecision());
                    value.put("score", result.getScore());
                    value.put("labels", result.getTags());
                    value.put("explanation", result.getExplanation());
                    value.put("aiStatus", result.getAiStatus());
                    value.put("priorityScore", result.getPriorityScore());
                    value.put("riskScore", result.getRiskScore());
                    value.put("resolved", result.getResolved());
                    return value;
                })
                .orElse(null);
    }

    private Long sameTargetOpenReportCount(ContentReport report) {
        return switch (report.getTargetType()) {
            case BLOG -> contentReportRepository.countByBlogIdAndStatusIn(
                    report.getBlog().getId(), ACTIVE_REPORT_STATUSES);
            case COMMENT -> contentReportRepository.countByCommentIdAndStatusIn(
                    report.getComment().getId(), ACTIVE_REPORT_STATUSES);
            case USER -> contentReportRepository.countByReportedUserUserIdAndStatusIn(
                    report.getReportedUser().getUserId(), ACTIVE_REPORT_STATUSES);
            case CAFE_PAGE -> contentReportRepository.countByCafePageIdAndStatusIn(
                    report.getCafePage().getId(), ACTIVE_REPORT_STATUSES);
        };
    }

    private String contentText(ContentReport report) {
        return switch (report.getTargetType()) {
            case BLOG -> report.getBlog().getContent();
            case COMMENT -> report.getComment().getContent();
            case USER -> userContentText(report.getReportedUser());
            case CAFE_PAGE -> cafePageContentText(report.getCafePage());
        };
    }

    private String userContentText(User user) {
        return String.join("\n", nonBlankValues(
                "username: " + nullToEmpty(user.getUserName()),
                "fullName: " + nullToEmpty(user.getUserFullName()),
                "description: " + nullToEmpty(user.getUserDescription())));
    }

    private String cafePageContentText(CafePage cafePage) {
        return String.join("\n", nonBlankValues(
                "name: " + nullToEmpty(cafePage.getName()),
                "address: " + nullToEmpty(cafePage.getAddress()),
                "description: " + nullToEmpty(cafePage.getDescription())));
    }

    private List<String> imageUrls(ContentReport report) {
        List<String> imageUrls = new ArrayList<>();
        switch (report.getTargetType()) {
            case BLOG -> {
                Blog blog = report.getBlog();
                if (blog.getImageUrls() != null) {
                    imageUrls.addAll(blog.getImageUrls());
                }
            }
            case COMMENT -> {
                Comment comment = report.getComment();
                if (comment.getImageUrls() != null) {
                    imageUrls.addAll(comment.getImageUrls());
                }
            }
            case USER -> addIfNotBlank(imageUrls, report.getReportedUser().getUserAvatar());
            case CAFE_PAGE -> {
                CafePage cafePage = report.getCafePage();
                addIfNotBlank(imageUrls, cafePage.getAvatarUrl());
                addIfNotBlank(imageUrls, cafePage.getCoverUrl());
            }
        }
        return imageUrls;
    }

    private UUID targetId(ContentReport report) {
        return switch (report.getTargetType()) {
            case BLOG -> report.getBlog() == null ? null : report.getBlog().getId();
            case COMMENT -> report.getComment() == null ? null : report.getComment().getId();
            case USER -> report.getReportedUser() == null ? null : report.getReportedUser().getUserId();
            case CAFE_PAGE -> report.getCafePage() == null ? null : report.getCafePage().getId();
        };
    }

    private AdminReportAiResolutionResponseDTO toResponse(AdminReportAiResolution resolution) {
        AdminReportAiResolutionResponseDTO response = new AdminReportAiResolutionResponseDTO();
        response.setId(resolution.getId());
        response.setContractVersion(resolution.getContractVersion() == null ? "legacy-v1" : resolution.getContractVersion());
        response.setCorrelationId(resolution.getCorrelationId());
        response.setAutomationMode(resolution.getAutomationMode() == null ? AUTOMATION_MODE : resolution.getAutomationMode());
        response.setRecommendationState(resolution.getReportDecision() == null
                ? null
                : resolution.getReportDecision().name());
        response.setContentReportId(resolution.getContentReport() == null
                ? null
                : resolution.getContentReport().getId());
        response.setTargetType(resolution.getTargetType());
        response.setTargetId(resolution.getTargetId());
        response.setReportDecision(resolution.getReportDecision());
        response.setTargetAction(resolution.getTargetAction());
        response.setConfidenceScore(resolution.getConfidenceScore());
        response.setRiskScore(resolution.getRiskScore());
        response.setLabels(resolution.getLabels());
        response.setRuleCode(resolution.getRuleCode());
        response.setExplanation(resolution.getExplanation());
        response.setModelName(resolution.getModelName());
        response.setCreatedAt(resolution.getCreatedAt());
        response.setFindings(resolution.getFindings());
        response.setEvidenceSummary(resolution.getEvidenceSummary());
        response.setBlockedReasons(resolution.getBlockedReasons());
        response.setEvidenceQuality(resolution.getEvidenceQuality());
        response.setEvidenceSufficiency(resolution.getEvidenceSufficiency());
        response.setViolationLikelihood(resolution.getViolationLikelihood());
        response.setHarmSeverity(resolution.getHarmSeverity());
        response.setActionRisk(resolution.getActionRisk());
        response.setPolicyVersion(resolution.getPolicyVersion());
        response.setRuleCatalogVersion(resolution.getRuleCatalogVersion());
        response.setPromptVersion(resolution.getPromptVersion());
        response.setWorkflowVersion(resolution.getWorkflowVersion());
        response.setTargetSnapshotHash(resolution.getTargetSnapshotHash());
        return response;
    }

    private List<String> nonBlankValues(String... values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                result.add(value);
            }
        }
        return result;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void addIfNotBlank(List<String> values, String value) {
        if (value != null && !value.isBlank()) {
            values.add(value);
        }
    }

    private static SimpleClientHttpRequestFactory requestFactory(int timeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        return requestFactory;
    }

    private record RawWebhookResponse(String body, String contentType, HttpHeaders headers) {
    }
}
