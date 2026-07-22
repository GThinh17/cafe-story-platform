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
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminReportAiResolutionServiceImpl implements AdminReportAiResolutionService {

    private static final Logger log = LoggerFactory.getLogger(AdminReportAiResolutionServiceImpl.class);
    private static final List<ReportStatus> ACTIVE_REPORT_STATUSES =
            List.of(ReportStatus.OPEN, ReportStatus.REVIEWING);

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
        AdminReportAiResolutionWebhookResponseDTO webhookResponse = callWebhook(webhookRequest);
        validateWebhookResponse(report.getTargetType(), webhookResponse);
        AdminReportAiResolution savedResolution = resolutionRepository.save(toEntity(report, webhookResponse));
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
                    "Admin report AI response parse failed contentType={} bodyPreview={}",
                    rawResponse.contentType(),
                    responseBody.substring(0, Math.min(responseBody.length(), 300)));
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
        return new AdminReportAiResolutionRequestDTO(
                report.getId(),
                report.getTargetType(),
                targetId(report),
                report.getReason() == null ? null : report.getReason().getCode(),
                report.getReasonSnapshot(),
                report.getReason() == null ? null : report.getReason().getSeverity(),
                report.getDescription(),
                contentText(report),
                imageUrls(report),
                report.getStatus(),
                existingModerationResult(report.getId()),
                sameTargetOpenReportCount(report));
    }

    private AdminReportAiResolution toEntity(
            ContentReport report,
            AdminReportAiResolutionWebhookResponseDTO webhookResponse) {
        AdminReportAiResolution resolution = new AdminReportAiResolution();
        resolution.setContentReport(report);
        resolution.setTargetType(report.getTargetType());
        resolution.setTargetId(targetId(report));
        resolution.setReportDecision(webhookResponse.getReportDecision());
        resolution.setTargetAction(webhookResponse.getTargetAction());
        resolution.setConfidenceScore(webhookResponse.getConfidenceScore());
        resolution.setRiskScore(webhookResponse.getRiskScore());
        resolution.setLabels(webhookResponse.getLabels() == null ? List.of() : webhookResponse.getLabels());
        resolution.setRuleCode(blankToNull(webhookResponse.getRuleCode()));
        resolution.setExplanation(blankToNull(webhookResponse.getExplanation()));
        resolution.setModelName(webhookResponse.getModelName().trim());
        resolution.setRawResponse(webhookResponse.getRawResponse() == null
                ? objectMapper.convertValue(webhookResponse, new TypeReference<LinkedHashMap<String, Object>>() {
                })
                : webhookResponse.getRawResponse());
        return resolution;
    }

    private void validateWebhookResponse(
            ReportTargetType targetType,
            AdminReportAiResolutionWebhookResponseDTO response) {
        if (response == null
                || response.getReportDecision() == null
                || response.getTargetAction() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Admin report AI resolution response is missing required fields");
        }
        if (!isTargetActionAllowed(targetType, response.getTargetAction())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Admin report AI resolution response targetAction is not valid for report target type");
        }
        if (!isDecisionActionCombinationAllowed(
                targetType,
                response.getReportDecision(),
                response.getTargetAction())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Admin report AI resolution response decision and targetAction are inconsistent");
        }
        if (!isValidScore(response.getConfidenceScore()) || !isValidScore(response.getRiskScore())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Admin report AI resolution response scores must be finite values from 0 to 100");
        }
        if (response.getExplanation() == null || response.getExplanation().isBlank()
                || response.getModelName() == null || response.getModelName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Admin report AI resolution response is missing audit metadata");
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

    private boolean isDecisionActionCombinationAllowed(
            ReportTargetType targetType,
            AdminReportAiReportDecision decision,
            AdminReportAiTargetAction action) {
        return switch (decision) {
            case NEEDS_MANUAL_REVIEW -> action == AdminReportAiTargetAction.NONE;
            case REJECT -> switch (targetType) {
                case BLOG, COMMENT -> action == AdminReportAiTargetAction.APPROVE;
                case USER, CAFE_PAGE -> action == AdminReportAiTargetAction.KEEP_ACTIVE;
            };
            case RESOLVE -> switch (targetType) {
                case BLOG, COMMENT -> action == AdminReportAiTargetAction.HIDE
                        || action == AdminReportAiTargetAction.REMOVE;
                case USER -> action == AdminReportAiTargetAction.SUSPEND_USER;
                case CAFE_PAGE -> action == AdminReportAiTargetAction.SUSPEND_PAGE;
            };
        };
    }

    private boolean isValidScore(Double score) {
        return score != null
                && !score.isNaN()
                && !score.isInfinite()
                && score >= 0.0
                && score <= 100.0;
    }

    private boolean isTargetActionAllowed(ReportTargetType targetType, AdminReportAiTargetAction targetAction) {
        if (targetAction == AdminReportAiTargetAction.NONE) {
            return true;
        }
        return switch (targetType) {
            case BLOG, COMMENT -> targetAction == AdminReportAiTargetAction.APPROVE
                    || targetAction == AdminReportAiTargetAction.HIDE
                    || targetAction == AdminReportAiTargetAction.REMOVE;
            case USER -> targetAction == AdminReportAiTargetAction.KEEP_ACTIVE
                    || targetAction == AdminReportAiTargetAction.SUSPEND_USER;
            case CAFE_PAGE -> targetAction == AdminReportAiTargetAction.KEEP_ACTIVE
                    || targetAction == AdminReportAiTargetAction.SUSPEND_PAGE;
        };
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
        response.setRawResponse(resolution.getRawResponse());
        response.setCreatedAt(resolution.getCreatedAt());
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
