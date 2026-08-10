package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.ReportModerationRequestDTO;
import com.cafestory.dto.responseDTO.ReportModerationJobResponseDTO;
import com.cafestory.dto.responseDTO.ReportModerationResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.ReportModerationJob;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.ReportModerationJobStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.repository.ReportModerationJobRepository;
import com.cafestory.service.serviceInterface.ReportModerationService;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportModerationServiceImpl implements ReportModerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportModerationServiceImpl.class);
    private static final List<ReportStatus> ACTIVE_REPORT_STATUSES =
            List.of(ReportStatus.OPEN, ReportStatus.REVIEWING);
    private static final List<ReportModerationJobStatus> RETRYABLE_STATUSES =
            List.of(ReportModerationJobStatus.PENDING, ReportModerationJobStatus.FAILED);
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final Duration STALE_PROCESSING_AFTER = Duration.ofMinutes(10);

    private final ReportModerationJobRepository jobRepository;
    private final AiModerationResultRepository moderationResultRepository;
    private final ContentReportRepository contentReportRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public ReportModerationServiceImpl(
            ReportModerationJobRepository jobRepository,
            AiModerationResultRepository moderationResultRepository,
            ContentReportRepository contentReportRepository,
            ObjectMapper objectMapper,
            PlatformTransactionManager transactionManager,
            @Value("${report.moderation.webhook-url:http://localhost:5678/webhook-test/cafestory-report-moderation}")
            String webhookUrl,
            @Value("${report.moderation.timeout-ms:10000}") int timeoutMs) {
        this(
                jobRepository,
                moderationResultRepository,
                contentReportRepository,
                objectMapper,
                new TransactionTemplate(transactionManager),
                RestClient.builder()
                        .baseUrl(webhookUrl)
                        .requestFactory(requestFactory(timeoutMs))
                        .build());
    }

    ReportModerationServiceImpl(
            ReportModerationJobRepository jobRepository,
            AiModerationResultRepository moderationResultRepository,
            ContentReportRepository contentReportRepository,
            ObjectMapper objectMapper,
            TransactionTemplate transactionTemplate,
            RestClient restClient) {
        this.jobRepository = jobRepository;
        this.moderationResultRepository = moderationResultRepository;
        this.contentReportRepository = contentReportRepository;
        this.objectMapper = objectMapper;
        this.transactionTemplate = transactionTemplate;
        this.restClient = restClient;
    }

    @Override
    public void enqueueReport(ContentReport report) {
        if (!isSupportedTarget(report)) {
            return;
        }
        if (jobRepository.existsByContentReportId(report.getId())) {
            return;
        }

        ReportModerationJob job = new ReportModerationJob();
        job.setContentReport(report);
        job.setTargetType(report.getTargetType());
        job.setTargetId(targetId(report));
        job.setStatus(ReportModerationJobStatus.PENDING);
        job.setAttemptCount(0);
        job.setMaxAttempts(DEFAULT_MAX_ATTEMPTS);
        job.setNextAttemptAt(LocalDateTime.now());
        applyPrioritySignals(job, report, null);
        jobRepository.save(job);
    }

    @Override
    public int processDueJobs(int limit) {
        int batchSize = Math.max(1, limit);
        List<ProcessingContext> contexts = claimDueJobs(batchSize);
        int processed = 0;

        for (ProcessingContext context : contexts) {
            long startedAt = System.nanoTime();
            try {
                ReportModerationResponseDTO response = callModerationWebhook(context.request());
                long durationMs = elapsedMillis(startedAt);
                completeJob(context.jobId(), response, durationMs);
                processed++;
            } catch (RuntimeException exception) {
                long durationMs = elapsedMillis(startedAt);
                failJob(context.jobId(), exception, durationMs);
                processed++;
            }
        }

        return processed;
    }

    @Override
    public ReportModerationJobResponseDTO retryReport(UUID reportId) {
        if (reportId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report id is required");
        }
        return transactionTemplate.execute(status -> {
            ReportModerationJob job = jobRepository.findByContentReportId(reportId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Report moderation job not found"));
            if (job.getTargetType() != ReportTargetType.BLOG && job.getTargetType() != ReportTargetType.COMMENT) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Report target is not AI moderated");
            }
            if (job.getStatus() == ReportModerationJobStatus.PROCESSING
                    || job.getStatus() == ReportModerationJobStatus.SUCCEEDED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Report moderation job cannot be retried");
            }
            job.setStatus(ReportModerationJobStatus.PENDING);
            job.setAttemptCount(0);
            job.setNextAttemptAt(LocalDateTime.now());
            job.setProcessingStartedAt(null);
            job.setLastError(null);
            return toJobResponse(jobRepository.save(job));
        });
    }

    @Override
    public Page<ReportModerationJobResponseDTO> getJobs(ReportModerationJobStatus status, Pageable pageable) {
        Page<ReportModerationJob> jobs = status == null
                ? jobRepository.findAll(pageable)
                : jobRepository.findByStatus(status, pageable);
        return jobs.map(this::toJobResponse);
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

    private List<ProcessingContext> claimDueJobs(int limit) {
        return transactionTemplate.execute(status -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime staleBefore = now.minus(STALE_PROCESSING_AFTER);
            List<ReportModerationJob> dueJobs = jobRepository.claimDueJobs(
                    RETRYABLE_STATUSES.stream().map(Enum::name).toList(),
                    ReportModerationJobStatus.PROCESSING.name(),
                    now,
                    staleBefore,
                    limit);

            return dueJobs.stream()
                    .map(job -> {
                        job.setStatus(ReportModerationJobStatus.PROCESSING);
                        job.setAttemptCount(job.getAttemptCount() + 1);
                        job.setProcessingStartedAt(now);
                        job.setLastError(null);
                        ReportModerationJob savedJob = jobRepository.save(job);
                        ContentReport report = savedJob.getContentReport();
                        return new ProcessingContext(savedJob.getId(), toRequest(report));
                    })
                    .toList();
        });
    }

    private void completeJob(UUID jobId, ReportModerationResponseDTO response, long durationMs) {
        transactionTemplate.executeWithoutResult(status -> {
            ReportModerationJob job = findJob(jobId);
            ContentReport report = job.getContentReport();
            if (!moderationResultRepository.existsByContentReportId(report.getId())) {
                applyPrioritySignals(job, report, response);
                moderationResultRepository.save(toModerationResult(report, job, response));
            }

            ModerationDecision decision = response.getDecision() == null
                    ? ModerationDecision.NEEDS_REVIEW
                    : response.getDecision();
            if (decision == ModerationDecision.NEEDS_REVIEW || decision == ModerationDecision.VIOLATION) {
                report.setStatus(ReportStatus.REVIEWING);
                contentReportRepository.save(report);
            }

            job.setStatus(ReportModerationJobStatus.SUCCEEDED);
            job.setProcessingStartedAt(null);
            job.setLastDurationMs(durationMs);
            job.setLastError(null);
            jobRepository.save(job);

            LOGGER.info(
                    "Report moderation job succeeded jobId={} reportId={} targetType={} attempt={} durationMs={} decision={}",
                    job.getId(),
                    report.getId(),
                    job.getTargetType(),
                    job.getAttemptCount(),
                    durationMs,
                    decision);
        });
    }

    private void failJob(UUID jobId, RuntimeException exception, long durationMs) {
        transactionTemplate.executeWithoutResult(status -> {
            ReportModerationJob job = findJob(jobId);
            boolean exhausted = job.getAttemptCount() >= job.getMaxAttempts();
            job.setStatus(exhausted ? ReportModerationJobStatus.DEAD_LETTER : ReportModerationJobStatus.FAILED);
            job.setProcessingStartedAt(null);
            job.setLastDurationMs(durationMs);
            job.setLastError(trimError(exception.getMessage()));
            if (!exhausted) {
                job.setNextAttemptAt(LocalDateTime.now().plus(backoffForAttempt(job.getAttemptCount())));
            }
            jobRepository.save(job);

            LOGGER.warn(
                    "Report moderation job failed jobId={} reportId={} targetType={} attempt={} status={} durationMs={} reason={}",
                    job.getId(),
                    job.getContentReport().getId(),
                    job.getTargetType(),
                    job.getAttemptCount(),
                    job.getStatus(),
                    durationMs,
                    exception.getMessage());
        });
    }

    private ReportModerationJob findJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report moderation job not found"));
    }

    private boolean isSupportedTarget(ContentReport report) {
        return report != null
                && report.getId() != null
                && (report.getTargetType() == ReportTargetType.BLOG
                || report.getTargetType() == ReportTargetType.COMMENT)
                && targetId(report) != null;
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
            ReportModerationJob job,
            ReportModerationResponseDTO response) {
        ModerationDecision decision = response.getDecision() == null
                ? ModerationDecision.NEEDS_REVIEW
                : response.getDecision();

        AiModerationResult result = new AiModerationResult();
        result.setContentReport(report);
        if (report.getTargetType() == ReportTargetType.BLOG) {
            result.setBlog(report.getBlog());
        } else if (report.getTargetType() == ReportTargetType.COMMENT) {
            result.setComment(report.getComment());
            result.setBlog(report.getComment() == null ? null : report.getComment().getBlog());
        }
        result.setCaption(contentText(report));
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
                ? "cafestory-report-n8n-openai"
                : response.getModelName());
        result.setRawResponse(rawResponse(response));
        result.setPriorityScore(job.getPriorityScore());
        result.setRiskScore(job.getRiskScore());
        result.setReasonSeveritySignal(job.getReasonSeveritySignal());
        result.setReportCountSignal(job.getReportCountSignal());
        result.setResolved(decision == ModerationDecision.SAFE);
        return result;
    }

    private void applyPrioritySignals(
            ReportModerationJob job,
            ContentReport report,
            ReportModerationResponseDTO response) {
        double riskScore = normalizeScore(response == null ? null : response.getScore());
        double reasonSeveritySignal = Math.max(0, nullToZero(report.getReason() == null
                ? null
                : report.getReason().getSeverity())) * 10.0;
        double reportCountSignal = Math.min(50.0, sameTargetReportCount(report) * 10.0);
        double targetSignal = report.getTargetType() == ReportTargetType.BLOG ? 10.0 : 5.0;

        job.setRiskScore(riskScore);
        job.setReasonSeveritySignal(reasonSeveritySignal);
        job.setReportCountSignal(reportCountSignal);
        job.setPriorityScore(riskScore + reasonSeveritySignal + reportCountSignal + targetSignal);
    }

    private long sameTargetReportCount(ContentReport report) {
        return switch (report.getTargetType()) {
            case BLOG -> report.getBlog() == null ? 0 :
                    contentReportRepository.countByBlogIdAndStatusIn(report.getBlog().getId(), ACTIVE_REPORT_STATUSES);
            case COMMENT -> report.getComment() == null ? 0 :
                    contentReportRepository.countByCommentIdAndStatusIn(report.getComment().getId(), ACTIVE_REPORT_STATUSES);
            case USER, CAFE_PAGE -> 0;
        };
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
            return report.getBlog().getImageUrls() == null ? List.of() : List.copyOf(report.getBlog().getImageUrls());
        }
        if (report.getTargetType() == ReportTargetType.COMMENT && report.getComment() != null) {
            return report.getComment().getImageUrls() == null
                    ? List.of()
                    : List.copyOf(report.getComment().getImageUrls());
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

    private ReportModerationJobResponseDTO toJobResponse(ReportModerationJob job) {
        ReportModerationJobResponseDTO response = new ReportModerationJobResponseDTO();
        response.setId(job.getId());
        response.setContentReportId(job.getContentReport() == null ? null : job.getContentReport().getId());
        response.setTargetType(job.getTargetType());
        response.setTargetId(job.getTargetId());
        response.setStatus(job.getStatus());
        response.setAttemptCount(job.getAttemptCount());
        response.setMaxAttempts(job.getMaxAttempts());
        response.setNextAttemptAt(job.getNextAttemptAt());
        response.setProcessingStartedAt(job.getProcessingStartedAt());
        response.setLastError(job.getLastError());
        response.setLastDurationMs(job.getLastDurationMs());
        response.setPriorityScore(job.getPriorityScore());
        response.setRiskScore(job.getRiskScore());
        response.setReasonSeveritySignal(job.getReasonSeveritySignal());
        response.setReportCountSignal(job.getReportCountSignal());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
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

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Duration backoffForAttempt(int attemptCount) {
        if (attemptCount <= 1) {
            return Duration.ofSeconds(30);
        }
        if (attemptCount == 2) {
            return Duration.ofMinutes(2);
        }
        return Duration.ofMinutes(10);
    }

    private String trimError(String message) {
        if (message == null || message.isBlank()) {
            return "Report moderation job failed";
        }
        return message.length() > 2000 ? message.substring(0, 2000) : message;
    }

    private long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
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

    private record ProcessingContext(UUID jobId, ReportModerationRequestDTO request) {
    }

    private record RawWebhookResponse(String body, String contentType) {
    }
}
