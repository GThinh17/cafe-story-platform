package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiAutoApplyJobResponseDTO;
import com.cafestory.entity.AdminReportAiAutoApplyJob;
import com.cafestory.entity.AdminReportAiResolution;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.enums.AdminReportAiAutoApplyJobStatus;
import com.cafestory.repository.AdminReportAiAutoApplyJobRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
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
import java.util.List;
import java.util.UUID;

@Service
public class AdminReportAiAutoApplyJobServiceImpl implements AdminReportAiAutoApplyJobService {

    public static final String RECOMMENDATION_ONLY_MODE = "A0_RECOMMEND_ONLY";
    private static final String AUTOMATION_DISABLED_WARNING =
            "Automation mode A0_RECOMMEND_ONLY is active; AI recommendations require an admin decision.";
    private static final String LEGACY_JOB_QUARANTINE_REASON =
            "Skipped by A0_RECOMMEND_ONLY safety mode; no report or target mutation was performed.";
    private static final Logger log = LoggerFactory.getLogger(AdminReportAiAutoApplyJobServiceImpl.class);

    private final AdminReportAiAutoApplyJobRepository jobRepository;
    private final ContentReportRepository contentReportRepository;
    private final TransactionTemplate transactionTemplate;
    private final String automationMode;

    public AdminReportAiAutoApplyJobServiceImpl(
            AdminReportAiAutoApplyJobRepository jobRepository,
            ContentReportRepository contentReportRepository,
            PlatformTransactionManager transactionManager,
            @Value("${admin.report.ai.automation-mode:A0_RECOMMEND_ONLY}") String automationMode) {
        this.jobRepository = jobRepository;
        this.contentReportRepository = contentReportRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.automationMode = requireSupportedAutomationMode(automationMode);
    }

    @Override
    public ScheduleResult scheduleIfRequested(
            ContentReport report,
            AdminReportAiResolution resolution,
            AdminReportAiResolutionCreateRequestDTO request,
            UUID adminUserId) {
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
            job.setStatus(AdminReportAiAutoApplyJobStatus.SKIPPED);
            job.setLastError(LEGACY_JOB_QUARANTINE_REASON);
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
            log.warn(
                    "Quarantined legacy Admin Report AI auto apply job jobId={} reportId={} mode={}",
                    job.getId(),
                    job.getContentReport() == null ? null : job.getContentReport().getId(),
                    automationMode);
        }));
        return dueJobs.size();
    }

    private AdminReportAiAutoApplyJob findJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auto apply job not found"));
    }

    private String requireSupportedAutomationMode(String configuredMode) {
        String normalized = configuredMode == null ? "" : configuredMode.trim();
        if (!RECOMMENDATION_ONLY_MODE.equals(normalized)) {
            throw new IllegalStateException(
                    "Unsupported admin.report.ai.automation-mode: " + configuredMode
                            + ". Sprint 1 only permits " + RECOMMENDATION_ONLY_MODE + ".");
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
}
