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
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.AdminReportAiResolutionRepository;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import com.cafestory.service.serviceInterface.AdminReportAiResolutionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import java.util.List;
import java.util.Map;
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

    @Autowired
    public AdminReportAiResolutionServiceImpl(
            AdminReportAiResolutionRepository resolutionRepository,
            ContentReportRepository contentReportRepository,
            AiModerationResultRepository moderationResultRepository,
            AdminReportAiAutoApplyJobService autoApplyJobService,
            ObjectMapper objectMapper,
            @Value("${admin.report.ai.webhook-url:http://localhost:5678/webhook/cafestory-admin-report-ai-resolution}")
            String webhookUrl,
            @Value("${admin.report.ai.timeout-ms:40000}") int timeoutMs) {
        this(
                resolutionRepository,
                contentReportRepository,
                moderationResultRepository,
                autoApplyJobService,
                objectMapper,
                RestClient.builder()
                        .baseUrl(webhookUrl)
                        .requestFactory(requestFactory(timeoutMs))
                        .build());
    }

    AdminReportAiResolutionServiceImpl(
            AdminReportAiResolutionRepository resolutionRepository,
            ContentReportRepository contentReportRepository,
            AiModerationResultRepository moderationResultRepository,
            AdminReportAiAutoApplyJobService autoApplyJobService,
            ObjectMapper objectMapper,
            RestClient restClient) {
        this.resolutionRepository = resolutionRepository;
        this.contentReportRepository = contentReportRepository;
        this.moderationResultRepository = moderationResultRepository;
        this.autoApplyJobService = autoApplyJobService;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
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
            return toResponse(existing.get());
        }

        AdminReportAiResolutionWebhookResponseDTO webhookResponse =
                isManualOnlyTarget(report.getTargetType())
                        ? localManualOnlyResponse(webhookRequest, "TARGET_DEEP_POLICY_NOT_IN_SPRINT1")
                        : callWebhook(webhookRequest);
        validateWebhookResponse(webhookRequest, webhookResponse);
        AdminReportAiResolution savedResolution =
                resolutionRepository.save(toEntity(report, webhookRequest, webhookResponse));
        AdminReportAiResolutionResponseDTO response = toResponse(savedResolution);
        if (autoApplyJobService != null && request != null && request.isAutoApplyEnabled()) {
            AdminReportAiAutoApplyJobService.ScheduleResult scheduleResult =
                    autoApplyJobService.scheduleIfRequested(report, savedResolution, request, adminUserId);
            response.setAutoApplyJob(scheduleResult.job());
            response.setAutoApplyWarning(scheduleResult.warning());
        }
        return response;
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
            rawResponse = restClient.post()
                    .uri("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .exchange((clientRequest, clientResponse) -> {
                        byte[] responseBytes = clientResponse.getBody().readAllBytes();
                        if (!clientResponse.getStatusCode().is2xxSuccessful()) {
                            throw new IllegalStateException("Admin report AI webhook returned "
                                    + clientResponse.getStatusCode());
                        }
                        MediaType contentType = clientResponse.getHeaders().getContentType();
                        return new RawWebhookResponse(
                                new String(responseBytes, StandardCharsets.UTF_8),
                                contentType == null ? "unknown" : contentType.toString());
                    });
        } catch (RuntimeException exception) {
            log.warn("Admin report AI webhook failed reportId={} reason={}", request.getReportId(), exception.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Admin report AI resolution service unavailable");
        }

        String responseBody = rawResponse.body();
        if (responseBody == null || responseBody.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Admin report AI resolution response is empty");
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
        request.setEvidence(evidence(report, request));
        request.setPolicyContext(policyContext(request.getReasonCode()));
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
            observable.put("parentBlogId", report.getComment().getBlog().getId());
            observable.put("parentBlogExcerpt", truncate(report.getComment().getBlog().getContent(), 500));
        }

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("targetType", report.getTargetType());
        snapshot.put("targetId", targetId(report));
        snapshot.put("snapshotVersion", "1");
        snapshot.put("capturedAt", OffsetDateTime.now(ZoneOffset.UTC));
        snapshot.put("observableFields", observable);
        snapshot.put("snapshotHash", sha256(toCanonicalJson(observable)));
        return snapshot;
    }

    private List<Map<String, Object>> evidence(
            ContentReport report,
            AdminReportAiResolutionRequestDTO request) {
        List<Map<String, Object>> evidence = new ArrayList<>();
        evidence.add(evidenceItem(
                "EV-TARGET-IDENTITY",
                "PLATFORM_RECORD",
                "Target " + report.getTargetType() + " id=" + targetId(report),
                "backend:content-report",
                "HIGH",
                "AVAILABLE"));
        evidence.add(evidenceItem(
                "EV-TARGET-CONTENT",
                "PLATFORM_RECORD",
                truncate(request.getContentText(), 4000),
                "backend:" + report.getTargetType().name().toLowerCase(),
                request.getContentText() == null || request.getContentText().isBlank() ? "UNUSABLE" : "HIGH",
                request.getContentText() == null || request.getContentText().isBlank()
                        ? "MISSING"
                        : "AVAILABLE"));
        evidence.add(evidenceItem(
                "EV-TARGET-STATE",
                "PLATFORM_RECORD",
                "status=" + targetStatus(report)
                        + "; createdAt=" + targetCreatedAt(report)
                        + "; updatedAt=" + targetUpdatedAt(report),
                "backend:" + report.getTargetType().name().toLowerCase(),
                "HIGH",
                "AVAILABLE"));
        evidence.add(evidenceItem(
                "EV-REASON-ROUTE",
                "REPORTER_CLAIM",
                "reasonCode=" + request.getReasonCode(),
                "backend:content-report-reason",
                "LOW",
                "AVAILABLE"));
        if (report.getTargetType() == ReportTargetType.COMMENT) {
            Blog parentBlog = report.getComment().getBlog();
            evidence.add(evidenceItem(
                    "EV-PARENT-CONTEXT",
                    "PLATFORM_RECORD",
                    parentBlog == null ? null : truncate(parentBlog.getContent(), 1000),
                    "backend:comment-parent-blog",
                    parentBlog == null ? "UNUSABLE" : "HIGH",
                    parentBlog == null ? "MISSING" : "AVAILABLE"));
        }
        if (!request.getImageUrls().isEmpty()) {
            evidence.add(evidenceItem(
                    "EV-TARGET-MEDIA",
                    "PLATFORM_RECORD",
                    "Media references exist but were not fetched or evaluated.",
                    "backend:media-reference",
                    "UNUSABLE",
                    "UNREADABLE_OR_NOT_EVALUATED"));
        }
        if (request.getExistingModerationResult() != null) {
            evidence.add(evidenceItem(
                    "EV-DERIVED-MODERATION",
                    "DERIVED_SIGNAL",
                    "Existing moderation signal is available; it is not independent evidence.",
                    "backend:ai-moderation-result",
                    "LOW",
                    "AVAILABLE"));
        }
        return evidence;
    }

    private Map<String, Object> policyContext(String reasonCode) {
        Map<String, Object> policy = new LinkedHashMap<>();
        policy.put("policyVersion", AdminReportAiPolicyCatalog.POLICY_VERSION);
        policy.put("ruleCatalogVersion", AdminReportAiPolicyCatalog.RULE_CATALOG_VERSION);
        policy.put("candidateRules", AdminReportAiPolicyCatalog.candidateRules(reasonCode));
        return policy;
    }

    private Map<String, Object> executionConstraints(
            ContentReport report,
            AdminReportAiResolutionRequestDTO request) {
        boolean missingContent = request.getContentText() == null || request.getContentText().isBlank();
        boolean mediaUnevaluated = request.getImageUrls() != null
                && !request.getImageUrls().isEmpty()
                && "INAPPROPRIATE_IMAGE".equalsIgnoreCase(request.getReasonCode());
        boolean parentMissing = report.getTargetType() == ReportTargetType.COMMENT
                && report.getComment().getBlog() == null;
        Map<String, Object> constraints = new LinkedHashMap<>();
        constraints.put("recommendationOnly", true);
        constraints.put("allowedCandidateActions", List.of("NO_ACTION", "KEEP_VISIBLE", "HIDE", "REMOVE"));
        constraints.put("criticalMissingBehavior", "NEEDS_MANUAL_REVIEW");
        constraints.put("criticalEvidenceMissing", missingContent || mediaUnevaluated || parentMissing);
        return constraints;
    }

    private Map<String, Object> evidenceItem(
            String evidenceId,
            String sourceType,
            String observation,
            String provenance,
            String quality,
            String availability) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("evidenceId", evidenceId);
        item.put("sourceType", sourceType);
        item.put("observation", observation);
        item.put("provenance", provenance);
        item.put("capturedAt", OffsetDateTime.now(ZoneOffset.UTC));
        item.put("quality", quality);
        item.put("availability", availability);
        return item;
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

    private record RawWebhookResponse(String body, String contentType) {
    }
}
