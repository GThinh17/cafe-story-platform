package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiAutoApplyJobResponseDTO;
import com.cafestory.entity.AdminReportAiAutoApplyJob;
import com.cafestory.entity.AdminReportAiResolution;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.enums.AdminReportAiAutoApplyJobStatus;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.AdminReportAiAutoApplyJobRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class AdminReportAiAutoApplyJobServiceImpl implements AdminReportAiAutoApplyJobService {

    public static final String RECOMMENDATION_ONLY_MODE = "A0_RECOMMEND_ONLY";
    public static final String AUTO_HIDE_BLOG_COMMENT_MODE = "A1_AUTO_HIDE_BLOG_COMMENT";
    private static final String AUTOMATION_DISABLED_WARNING =
            "Automation mode A0_RECOMMEND_ONLY is active; AI recommendations require an admin decision.";
    private static final String LEGACY_JOB_QUARANTINE_REASON =
            "Skipped by A0_RECOMMEND_ONLY safety mode; no report or target mutation was performed.";
    private static final List<ReportStatus> ACTIVE_REPORT_STATUSES =
            List.of(ReportStatus.OPEN, ReportStatus.REVIEWING);
    private static final Logger log = LoggerFactory.getLogger(AdminReportAiAutoApplyJobServiceImpl.class);

    private final AdminReportAiAutoApplyJobRepository jobRepository;
    private final ContentReportRepository contentReportRepository;
    private final TransactionTemplate transactionTemplate;
    private final String automationMode;
    private final int defaultDelayMinutes;

    public AdminReportAiAutoApplyJobServiceImpl(
            AdminReportAiAutoApplyJobRepository jobRepository,
            ContentReportRepository contentReportRepository,
            PlatformTransactionManager transactionManager,
            @Value("${admin.report.ai.automation-mode:A0_RECOMMEND_ONLY}") String automationMode) {
        this(
                jobRepository,
                contentReportRepository,
                transactionManager,
                automationMode,
                5);
    }

    @Autowired
    public AdminReportAiAutoApplyJobServiceImpl(
            AdminReportAiAutoApplyJobRepository jobRepository,
            ContentReportRepository contentReportRepository,
            PlatformTransactionManager transactionManager,
            @Value("${admin.report.ai.automation-mode:A0_RECOMMEND_ONLY}") String automationMode,
            @Value("${admin.report.ai.auto-apply.delay-minutes:5}") int defaultDelayMinutes) {
        this.jobRepository = jobRepository;
        this.contentReportRepository = contentReportRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.automationMode = requireSupportedAutomationMode(automationMode);
        this.defaultDelayMinutes = Math.max(1, defaultDelayMinutes);
    }

    @Override
    public ScheduleResult scheduleIfRequested(
            ContentReport report,
            AdminReportAiResolution resolution,
            AdminReportAiResolutionCreateRequestDTO request,
            UUID adminUserId) {
        if (RECOMMENDATION_ONLY_MODE.equals(automationMode)) {
            if (request == null || !request.isAutoApplyEnabled()) {
                return new ScheduleResult(null, null);
            }
            log.warn(
                    "Blocked Admin Report AI auto apply request reportId={} resolutionId={} mode={}",
                    report == null ? null : report.getId(),
                    resolution == null ? null : resolution.getId(),
                    automationMode);
            return new ScheduleResult(null, AUTOMATION_DISABLED_WARNING);
        }
        EligibilityResult eligibility = autoHideEligibility(report, resolution);
        if (!eligibility.eligible()) {
            return new ScheduleResult(
                    null,
                    request != null && request.isAutoApplyEnabled()
                            ? "Auto hide not scheduled: " + eligibility.reason()
                            : null);
        }

        int delayMinutes = request == null || request.getAutoApplyDelayMinutes() == null
                ? defaultDelayMinutes
                : request.getAutoApplyDelayMinutes();
        AdminReportAiAutoApplyJob job = new AdminReportAiAutoApplyJob();
        job.setContentReport(report);
        job.setAiResolution(resolution);
        job.setTargetType(resolution.getTargetType());
        job.setTargetId(resolution.getTargetId());
        job.setReportDecision(resolution.getReportDecision());
        job.setTargetAction(resolution.getTargetAction());
        job.setConfidenceScore(resolution.getConfidenceScore());
        job.setRiskScore(resolution.getRiskScore());
        job.setStatus(AdminReportAiAutoApplyJobStatus.SCHEDULED);
        job.setScheduledAt(LocalDateTime.now().plusMinutes(Math.max(1, delayMinutes)));
        job.setCreatedByAdminUserId(adminUserId);
        return new ScheduleResult(toResponse(jobRepository.save(job)), null);
    }

    @Override
    public Page<AdminReportAiAutoApplyJobResponseDTO> getJobs(UUID reportId, Pageable pageable) {
        if (reportId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report id is required");
        }
        if (!contentReportRepository.existsById(reportId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found");
        }
        return jobRepository.findByContentReportId(reportId, pageable)
                .map(this::toResponse);
    }

    @Override
    public AdminReportAiAutoApplyJobResponseDTO cancelJob(UUID jobId, UUID adminUserId) {
        if (jobId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Auto apply job id is required");
        }
        return transactionTemplate.execute(status -> {
            AdminReportAiAutoApplyJob job = findJob(jobId);
            if (job.getStatus() != AdminReportAiAutoApplyJobStatus.SCHEDULED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Auto apply job cannot be cancelled");
            }
            job.setStatus(AdminReportAiAutoApplyJobStatus.CANCELLED);
            job.setCancelledAt(LocalDateTime.now());
            job.setCancelledByAdminUserId(adminUserId);
            job.setCancellationReason("ADMIN_CANCELLED");
            return toResponse(jobRepository.save(job));
        });
    }

    @Override
    public int processDueJobs(int limit) {
        int batchSize = Math.max(1, limit);
        List<AdminReportAiAutoApplyJob> dueJobs = transactionTemplate.execute(status ->
                jobRepository.claimDueJobs(
                        AdminReportAiAutoApplyJobStatus.SCHEDULED.name(),
                        LocalDateTime.now(),
                        batchSize));
        if (dueJobs == null || dueJobs.isEmpty()) {
            return 0;
        }

        transactionTemplate.executeWithoutResult(status -> dueJobs.forEach(job -> {
            if (RECOMMENDATION_ONLY_MODE.equals(automationMode)) {
                skipJob(job, LEGACY_JOB_QUARANTINE_REASON);
                log.warn(
                        "Quarantined legacy Admin Report AI auto apply job jobId={} reportId={} mode={}",
                        job.getId(),
                        job.getContentReport() == null ? null : job.getContentReport().getId(),
                        automationMode);
                return;
            }
            applyAutoHide(job);
        }));
        return dueJobs.size();
    }

    private void applyAutoHide(AdminReportAiAutoApplyJob job) {
        job.setStatus(AdminReportAiAutoApplyJobStatus.APPLYING);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        EligibilityResult eligibility = autoHideEligibility(job.getContentReport(), job.getAiResolution());
        if (!eligibility.eligible()) {
            skipJob(job, eligibility.reason());
            return;
        }

        ContentReport report = job.getContentReport();
        if (!Objects.equals(job.getTargetType(), report.getTargetType())
                || !Objects.equals(job.getTargetId(), targetId(report))) {
            skipJob(job, "Target snapshot no longer matches the scheduled job");
            return;
        }
        if (report.getTargetType() == ReportTargetType.BLOG) {
            report.getBlog().setStatus(PostStatus.HIDDEN);
        } else if (report.getTargetType() == ReportTargetType.COMMENT) {
            report.getComment().setStatus(PostStatus.HIDDEN);
        } else {
            skipJob(job, "Target type is not eligible for auto hide");
            return;
        }
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedAt(LocalDateTime.now());
        job.setStatus(AdminReportAiAutoApplyJobStatus.APPLIED);
        job.setAppliedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        contentReportRepository.save(report);
        jobRepository.save(job);
    }

    private void skipJob(AdminReportAiAutoApplyJob job, String reason) {
        job.setStatus(AdminReportAiAutoApplyJobStatus.SKIPPED);
        job.setLastError(reason);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    private EligibilityResult autoHideEligibility(ContentReport report, AdminReportAiResolution resolution) {
        if (report == null || resolution == null) {
            return EligibilityResult.skipped("Report or AI resolution is unavailable");
        }
        if (report.getTargetType() != ReportTargetType.BLOG && report.getTargetType() != ReportTargetType.COMMENT) {
            return EligibilityResult.skipped("Only BLOG and COMMENT targets are eligible");
        }
        if (!ACTIVE_REPORT_STATUSES.contains(report.getStatus())) {
            return EligibilityResult.skipped("Report is no longer OPEN or REVIEWING");
        }
        if (!Objects.equals(report.getTargetType(), resolution.getTargetType())
                || !Objects.equals(targetId(report), resolution.getTargetId())) {
            return EligibilityResult.skipped("Report target no longer matches the AI resolution");
        }
        if (targetStatus(report) != PostStatus.PUBLISHED) {
            return EligibilityResult.skipped("Target is no longer published");
        }
        if (resolution.getReportDecision() != AdminReportAiReportDecision.RESOLVE
                || resolution.getTargetAction() != AdminReportAiTargetAction.HIDE) {
            return EligibilityResult.skipped("AI did not recommend RESOLVE + HIDE");
        }
        if (!"HIGH".equals(resolution.getEvidenceQuality())
                || !"SUFFICIENT".equals(resolution.getEvidenceSufficiency())
                || !"HIGH".equals(resolution.getViolationLikelihood())) {
            return EligibilityResult.skipped("AI evidence gates are not HIGH/SUFFICIENT/HIGH");
        }
        if (resolution.getBlockedReasons() != null && !resolution.getBlockedReasons().isEmpty()) {
            return EligibilityResult.skipped("AI response has blocked reasons");
        }
        if (hasMissingEvidence(resolution.getEvidenceSummary())) {
            return EligibilityResult.skipped("AI response has missing evidence references");
        }
        if (!materialFindingsAreSubstantiatedWithEvidence(resolution.getFindings())) {
            return EligibilityResult.skipped("Material findings are missing valid evidence references");
        }
        return EligibilityResult.allowed();
    }

    private boolean hasMissingEvidence(Map<String, Object> evidenceSummary) {
        if (evidenceSummary == null || evidenceSummary.isEmpty()) {
            return true;
        }
        Object missingEvidenceIds = evidenceSummary.get("missingEvidenceIds");
        return missingEvidenceIds instanceof Collection<?> values && !values.isEmpty();
    }

    private boolean materialFindingsAreSubstantiatedWithEvidence(List<Map<String, Object>> findings) {
        if (findings == null || findings.isEmpty()) {
            return false;
        }
        boolean substantiated = false;
        for (Map<String, Object> finding : findings) {
            Object ruleId = finding.get("ruleId");
            Object outcome = finding.get("outcome");
            Object evidenceIds = finding.get("evidenceIds");
            if (!(ruleId instanceof String rule) || rule.isBlank()
                    || !(outcome instanceof String result) || result.isBlank()
                    || !(evidenceIds instanceof Collection<?> references) || references.isEmpty()
                    || references.stream().noneMatch(value -> value instanceof String id && !id.isBlank())) {
                return false;
            }
            if ("SUBSTANTIATED".equals(result)) {
                substantiated = true;
            }
        }
        return substantiated;
    }

    private PostStatus targetStatus(ContentReport report) {
        return switch (report.getTargetType()) {
            case BLOG -> report.getBlog() == null ? null : report.getBlog().getStatus();
            case COMMENT -> report.getComment() == null ? null : report.getComment().getStatus();
            case USER, CAFE_PAGE -> null;
        };
    }

    private UUID targetId(ContentReport report) {
        return switch (report.getTargetType()) {
            case BLOG -> report.getBlog() == null ? null : report.getBlog().getId();
            case COMMENT -> report.getComment() == null ? null : report.getComment().getId();
            case USER -> report.getReportedUser() == null ? null : report.getReportedUser().getUserId();
            case CAFE_PAGE -> report.getCafePage() == null ? null : report.getCafePage().getId();
        };
    }

    private AdminReportAiAutoApplyJob findJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auto apply job not found"));
    }

    private String requireSupportedAutomationMode(String configuredMode) {
        String normalized = configuredMode == null ? "" : configuredMode.trim();
        if (!RECOMMENDATION_ONLY_MODE.equals(normalized)
                && !AUTO_HIDE_BLOG_COMMENT_MODE.equals(normalized)) {
            throw new IllegalStateException(
                    "Unsupported admin.report.ai.automation-mode: " + configuredMode
                            + ". Supported modes: " + RECOMMENDATION_ONLY_MODE
                            + ", " + AUTO_HIDE_BLOG_COMMENT_MODE + ".");
        }
        return normalized;
    }

    private AdminReportAiAutoApplyJobResponseDTO toResponse(AdminReportAiAutoApplyJob job) {
        AdminReportAiAutoApplyJobResponseDTO response = new AdminReportAiAutoApplyJobResponseDTO();
        response.setId(job.getId());
        response.setContentReportId(job.getContentReport() == null ? null : job.getContentReport().getId());
        response.setAiResolutionId(job.getAiResolution() == null ? null : job.getAiResolution().getId());
        response.setTargetType(job.getTargetType());
        response.setTargetId(job.getTargetId());
        response.setStatus(job.getStatus());
        response.setReportDecision(job.getReportDecision());
        response.setTargetAction(job.getTargetAction());
        response.setConfidenceScore(job.getConfidenceScore());
        response.setRiskScore(job.getRiskScore());
        response.setScheduledAt(job.getScheduledAt());
        response.setAppliedAt(job.getAppliedAt());
        response.setCancelledAt(job.getCancelledAt());
        response.setLastError(job.getLastError());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }

    private record EligibilityResult(boolean eligible, String reason) {
        static EligibilityResult allowed() {
            return new EligibilityResult(true, null);
        }

        static EligibilityResult skipped(String reason) {
            return new EligibilityResult(false, reason);
        }
    }
}
