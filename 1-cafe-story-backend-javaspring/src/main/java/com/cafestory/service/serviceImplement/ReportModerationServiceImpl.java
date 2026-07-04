package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.ReportModerationRequestDTO;
import com.cafestory.dto.responseDTO.ReportModerationResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.Comment;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.service.serviceInterface.ReportModerationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportModerationServiceImpl implements ReportModerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportModerationServiceImpl.class);
    private static final String FALLBACK_MODEL_NAME = "cafestory-report-n8n-openai";
    private static final String FALLBACK_EXPLANATION =
            "Report moderation service unavailable. Report requires admin review.";

    private final AiModerationResultRepository moderationResultRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public ReportModerationServiceImpl(
            AiModerationResultRepository moderationResultRepository,
            ObjectMapper objectMapper,
            @Value("${report.moderation.webhook-url:http://localhost:5678/webhook-test/cafestory-report-moderation}")
            String webhookUrl,
            @Value("${report.moderation.timeout-ms:10000}") int timeoutMs) {
        this(
                moderationResultRepository,
                objectMapper,
                RestClient.builder()
                        .baseUrl(webhookUrl)
                        .requestFactory(requestFactory(timeoutMs))
                        .build());
    }

    ReportModerationServiceImpl(
            AiModerationResultRepository moderationResultRepository,
            ObjectMapper objectMapper,
            RestClient restClient) {
        this.moderationResultRepository = moderationResultRepository;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    @Override
    @Transactional
    public void moderateReport(ContentReport report) {
        if (!isSupportedTarget(report)) {
            return;
        }

        ReportModerationRequestDTO request = toRequest(report);
        ReportModerationResponseDTO response;
        try {
            response = callModerationWebhook(request);
        } catch (RuntimeException exception) {
            LOGGER.warn(
                    "Report moderation webhook failed reportId={} targetType={} targetId={} reason={}",
                    report.getId(),
                    report.getTargetType(),
                    targetId(report),
                    exception.getMessage());
            response = fallbackResponse(exception);
        }

        moderationResultRepository.save(toModerationResult(report, request, response));
    }

    protected ReportModerationResponseDTO callModerationWebhook(ReportModerationRequestDTO request) {
        RawWebhookResponse rawResponse = restClient.post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange((clientRequest, clientResponse) -> {
                    byte[] responseBytes = clientResponse.getBody().readAllBytes();
                    if (!clientResponse.getStatusCode().is2xxSuccessful()) {
                        throw new IllegalStateException("Report moderation webhook returned "
                                + clientResponse.getStatusCode());
                    }
                    MediaType contentType = clientResponse.getHeaders().getContentType();
                    return new RawWebhookResponse(
                            new String(responseBytes, StandardCharsets.UTF_8),
                            contentType == null ? "unknown" : contentType.toString());
                });

        String responseBody = rawResponse.body();
        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("Report moderation response is empty");
        }

        try {
            return objectMapper.readValue(responseBody, ReportModerationResponseDTO.class);
        } catch (IOException exception) {
            LOGGER.warn(
                    "Report moderation response parse failed contentType={} bodyPreview={}",
                    rawResponse.contentType(),
                    responseBody.substring(0, Math.min(responseBody.length(), 300)));
            throw new IllegalStateException("Report moderation response is not valid JSON", exception);
        }
    }

    private boolean isSupportedTarget(ContentReport report) {
        return report != null
                && (report.getTargetType() == ReportTargetType.BLOG
                || report.getTargetType() == ReportTargetType.COMMENT);
    }

    private ReportModerationRequestDTO toRequest(ContentReport report) {
        return new ReportModerationRequestDTO(
                report.getId(),
                report.getTargetType(),
                targetId(report),
                report.getReason() == null ? null : report.getReason().getCode(),
                report.getReasonSnapshot(),
                report.getDescription(),
                contentText(report),
                imageUrls(report));
    }

    private AiModerationResult toModerationResult(
            ContentReport report,
            ReportModerationRequestDTO request,
            ReportModerationResponseDTO response) {
        ModerationDecision decision = response.getDecision() == null
                ? ModerationDecision.NEEDS_REVIEW
                : response.getDecision();

        AiModerationResult result = new AiModerationResult();
        if (report.getTargetType() == ReportTargetType.BLOG) {
            result.setBlog(report.getBlog());
        } else if (report.getTargetType() == ReportTargetType.COMMENT) {
            result.setComment(report.getComment());
            result.setBlog(report.getComment() == null ? null : report.getComment().getBlog());
        }
        result.setCaption(request.getContentText());
        result.setScore(normalizeScore(response.getScore()));
        result.setDecision(decision);
        result.setCaptionScore(scoreToInteger(response.getScore()));
        result.setCaptionReason(response.getExplanation());
        result.setImageScore(null);
        result.setImageReason(null);
        result.setTags(response.getLabels() == null ? List.of() : response.getLabels());
        result.setAiStatus(decision.name());
        result.setLabels(String.join(",", result.getTags()));
        result.setExplanation(response.getExplanation());
        result.setModelName(response.getModelName() == null || response.getModelName().isBlank()
                ? FALLBACK_MODEL_NAME
                : response.getModelName());
        result.setRawResponse(rawResponse(response));
        result.setResolved(decision == ModerationDecision.SAFE);
        return result;
    }

    private ReportModerationResponseDTO fallbackResponse(RuntimeException exception) {
        ReportModerationResponseDTO response = new ReportModerationResponseDTO();
        response.setDecision(ModerationDecision.NEEDS_REVIEW);
        response.setScore(0.0);
        response.setLabels(List.of());
        response.setExplanation(FALLBACK_EXPLANATION + " " + exception.getMessage());
        response.setModelName(FALLBACK_MODEL_NAME);
        return response;
    }

    private String contentText(ContentReport report) {
        if (report.getTargetType() == ReportTargetType.BLOG && report.getBlog() != null) {
            return report.getBlog().getContent();
        }
        if (report.getTargetType() == ReportTargetType.COMMENT && report.getComment() != null) {
            return report.getComment().getContent();
        }
        return null;
    }

    private List<String> imageUrls(ContentReport report) {
        if (report.getTargetType() == ReportTargetType.BLOG && report.getBlog() != null) {
            return report.getBlog().getImageUrls() == null ? List.of() : report.getBlog().getImageUrls();
        }
        if (report.getTargetType() == ReportTargetType.COMMENT && report.getComment() != null) {
            return report.getComment().getImageUrls() == null ? List.of() : report.getComment().getImageUrls();
        }
        return List.of();
    }

    private UUID targetId(ContentReport report) {
        return switch (report.getTargetType()) {
            case BLOG -> report.getBlog() == null ? null : report.getBlog().getId();
            case COMMENT -> report.getComment() == null ? null : report.getComment().getId();
            case USER -> report.getReportedUser() == null ? null : report.getReportedUser().getUserId();
            case CAFE_PAGE -> report.getCafePage() == null ? null : report.getCafePage().getId();
        };
    }

    private Double normalizeScore(Double score) {
        if (score == null || score.isNaN() || score.isInfinite()) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(100.0, score));
    }

    private Integer scoreToInteger(Double score) {
        return (int) Math.round(normalizeScore(score));
    }

    private Map<String, Object> rawResponse(ReportModerationResponseDTO response) {
        return objectMapper.convertValue(response, new TypeReference<LinkedHashMap<String, Object>>() {
        });
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
