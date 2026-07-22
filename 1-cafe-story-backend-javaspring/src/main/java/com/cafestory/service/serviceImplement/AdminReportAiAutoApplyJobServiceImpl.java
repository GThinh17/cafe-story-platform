package com.cafestory.service.serviceImplement;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiAutoApplyJobResponseDTO;
import com.cafestory.entity.AdminReportAiAutoApplyJob;
import com.cafestory.entity.AdminReportAiResolution;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Comment;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.AdminReportAiAutoApplyJobStatus;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.PageStatus;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.AdminReportAiAutoApplyJobRepository;
import com.cafestory.repository.BlogRepository;
import com.cafestory.repository.CafePageRepository;
import com.cafestory.repository.CommentRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.repository.UserRepository;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AdminReportAiAutoApplyJobServiceImpl.class);
    private static final double MIN_CONFIDENCE = 80.0;
    private static final double MIN_RESOLVE_RISK = 70.0;
    private static final double MAX_REJECT_RISK = 30.0;
    private static final List<ReportStatus> ACTIVE_REPORT_STATUSES =
            List.of(ReportStatus.OPEN, ReportStatus.REVIEWING);
    private static final List<AdminReportAiAutoApplyJobStatus> ACTIVE_JOB_STATUSES =
            List.of(AdminReportAiAutoApplyJobStatus.SCHEDULED, AdminReportAiAutoApplyJobStatus.APPLYING);

    private final AdminReportAiAutoApplyJobRepository jobRepository;
    private final ContentReportRepository contentReportRepository;
    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CafePageRepository cafePageRepository;
    private final TransactionTemplate transactionTemplate;

    public AdminReportAiAutoApplyJobServiceImpl(
            AdminReportAiAutoApplyJobRepository jobRepository,
            ContentReportRepository contentReportRepository,
            BlogRepository blogRepository,
            CommentRepository commentRepository,
            UserRepository userRepository,
            CafePageRepository cafePageRepository,
            PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.contentReportRepository = contentReportRepository;
        this.blogRepository = blogRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.cafePageRepository = cafePageRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
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

        String safetyWarning = safetyWarning(resolution);
        if (safetyWarning != null) {
            return new ScheduleResult(null, safetyWarning);
        }

        Integer delayMinutes = request.getAutoApplyDelayMinutes();
        if (delayMinutes == null) {
            return new ScheduleResult(null, "Auto apply delay is required.");
        }

        return transactionTemplate.execute(status -> {
            AdminReportAiAutoApplyJob activeJob = jobRepository
                    .findFirstByContentReportIdAndStatusInOrderByCreatedAtDesc(report.getId(), ACTIVE_JOB_STATUSES)
                    .orElse(null);
            if (activeJob != null && activeJob.getStatus() == AdminReportAiAutoApplyJobStatus.APPLYING) {
                return new ScheduleResult(null, "An auto apply job is already applying for this report.");
            }
            if (activeJob != null) {
                cancelJob(activeJob, adminUserId, "REPLACED_BY_NEWER_RECOMMENDATION");
                jobRepository.save(activeJob);
            }

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
            job.setScheduledAt(LocalDateTime.now().plusMinutes(delayMinutes));
            job.setCreatedByAdminUserId(adminUserId);

            return new ScheduleResult(toResponse(jobRepository.save(job)), null);
        });
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
            cancelJob(job, adminUserId, "ADMIN_CANCELLED");
            return toResponse(jobRepository.save(job));
        });
    }

    @Override
    public int processDueJobs(int limit) {
        int batchSize = Math.max(1, limit);
        List<UUID> jobIds = claimDueJobs(batchSize);
        int processed = 0;

        for (UUID jobId : jobIds) {
            try {
                applyJob(jobId);
            } catch (RuntimeException exception) {
                markFailed(jobId, exception);
            }
            processed++;
        }

        return processed;
    }

    private List<UUID> claimDueJobs(int limit) {
        return transactionTemplate.execute(status -> {
            List<AdminReportAiAutoApplyJob> dueJobs = jobRepository.claimDueJobs(
                    AdminReportAiAutoApplyJobStatus.SCHEDULED.name(),
                    LocalDateTime.now(),
                    limit);
            LocalDateTime now = LocalDateTime.now();
            dueJobs.forEach(job -> {
                job.setStatus(AdminReportAiAutoApplyJobStatus.APPLYING);
                job.setLastError(null);
                job.setUpdatedAt(now);
                jobRepository.save(job);
            });
            return dueJobs.stream().map(AdminReportAiAutoApplyJob::getId).toList();
        });
    }

    private void applyJob(UUID jobId) {
        transactionTemplate.executeWithoutResult(status -> {
            AdminReportAiAutoApplyJob job = findJob(jobId);
            ContentReport report = job.getContentReport();
            if (report == null || !ACTIVE_REPORT_STATUSES.contains(report.getStatus())) {
                markSkipped(job, "Report is no longer open for auto apply");
                return;
            }
            String staleTargetReason = staleTargetReason(job, report);
            if (staleTargetReason != null) {
                markSkipped(job, staleTargetReason);
                return;
            }

            if (job.getReportDecision() == AdminReportAiReportDecision.REJECT) {
                rejectReport(report);
            } else {
                applyTargetAction(job, report);
                resolveReport(report);
            }

            job.setStatus(AdminReportAiAutoApplyJobStatus.APPLIED);
            job.setAppliedAt(LocalDateTime.now());
            job.setLastError(null);
            jobRepository.save(job);
            log.info(
                    "AdminReportAiAutoApplyJob applied jobId={} reportId={} aiResolutionId={} targetType={} targetAction={} status={}",
                    job.getId(),
                    report.getId(),
                    job.getAiResolution() == null ? null : job.getAiResolution().getId(),
                    job.getTargetType(),
                    job.getTargetAction(),
                    job.getStatus());
        });
    }

    private String staleTargetReason(AdminReportAiAutoApplyJob job, ContentReport report) {
        UUID currentTargetId = currentTargetId(report);
        if (currentTargetId == null || !currentTargetId.equals(job.getTargetId())) {
            return "Report target no longer matches the AI recommendation";
        }

        LocalDateTime recommendedAt = job.getAiResolution() == null
                ? null
                : job.getAiResolution().getCreatedAt();
        return switch (job.getTargetType()) {
            case BLOG -> staleBlogReason(report.getBlog(), recommendedAt);
            case COMMENT -> staleCommentReason(report.getComment(), recommendedAt);
            case USER -> report.getReportedUser() == null || !Boolean.TRUE.equals(report.getReportedUser().getAccountStatus())
                    ? "Reported user is no longer active"
                    : null;
            case CAFE_PAGE -> staleCafePageReason(report.getCafePage(), recommendedAt);
        };
    }

    private String staleBlogReason(Blog blog, LocalDateTime recommendedAt) {
        if (blog == null || blog.getStatus() != PostStatus.PUBLISHED) {
            return "Reported blog is no longer published";
        }
        return changedAfterRecommendation(blog.getUpdatedAt(), recommendedAt)
                ? "Reported blog changed after the AI recommendation"
                : null;
    }

    private String staleCommentReason(Comment comment, LocalDateTime recommendedAt) {
        if (comment == null || comment.getStatus() != PostStatus.PUBLISHED) {
            return "Reported comment is no longer published";
        }
        return changedAfterRecommendation(comment.getUpdatedAt(), recommendedAt)
                ? "Reported comment changed after the AI recommendation"
                : null;
    }

    private String staleCafePageReason(CafePage cafePage, LocalDateTime recommendedAt) {
        if (cafePage == null
                || cafePage.getStatus() != PageStatus.ACTIVE
                || !Boolean.TRUE.equals(cafePage.getPageActive())) {
            return "Reported cafe page is no longer active";
        }
        return changedAfterRecommendation(cafePage.getUpdatedAt(), recommendedAt)
                ? "Reported cafe page changed after the AI recommendation"
                : null;
    }

    private boolean changedAfterRecommendation(LocalDateTime targetUpdatedAt, LocalDateTime recommendedAt) {
        return targetUpdatedAt != null
                && recommendedAt != null
                && targetUpdatedAt.isAfter(recommendedAt);
    }

    private UUID currentTargetId(ContentReport report) {
        return switch (report.getTargetType()) {
            case BLOG -> report.getBlog() == null ? null : report.getBlog().getId();
            case COMMENT -> report.getComment() == null ? null : report.getComment().getId();
            case USER -> report.getReportedUser() == null ? null : report.getReportedUser().getUserId();
            case CAFE_PAGE -> report.getCafePage() == null ? null : report.getCafePage().getId();
        };
    }

    private void applyTargetAction(AdminReportAiAutoApplyJob job, ContentReport report) {
        switch (job.getTargetType()) {
            case BLOG -> applyBlogAction(report.getBlog(), job.getTargetAction());
            case COMMENT -> applyCommentAction(report.getComment(), job.getTargetAction());
            case USER -> applyUserAction(report.getReportedUser(), job.getTargetAction());
            case CAFE_PAGE -> applyCafePageAction(report.getCafePage(), job.getTargetAction());
        }
    }

    private void applyBlogAction(Blog blog, AdminReportAiTargetAction action) {
        if (action == AdminReportAiTargetAction.HIDE) {
            blog.setStatus(PostStatus.HIDDEN);
        } else if (action == AdminReportAiTargetAction.REMOVE) {
            blog.setStatus(PostStatus.REMOVED);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Auto apply action is not valid for blog");
        }
        blogRepository.save(blog);
    }

    private void applyCommentAction(Comment comment, AdminReportAiTargetAction action) {
        if (action == AdminReportAiTargetAction.HIDE) {
            comment.setStatus(PostStatus.HIDDEN);
        } else if (action == AdminReportAiTargetAction.REMOVE) {
            comment.setStatus(PostStatus.REMOVED);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Auto apply action is not valid for comment");
        }
        commentRepository.save(comment);
    }

    private void applyUserAction(User user, AdminReportAiTargetAction action) {
        if (action != AdminReportAiTargetAction.SUSPEND_USER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Auto apply action is not valid for user");
        }
        user.setAccountStatus(false);
        userRepository.save(user);
    }

    private void applyCafePageAction(CafePage cafePage, AdminReportAiTargetAction action) {
        if (action != AdminReportAiTargetAction.SUSPEND_PAGE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Auto apply action is not valid for cafe page");
        }
        cafePage.setStatus(PageStatus.SUSPENDED);
        cafePage.setPageActive(false);
        cafePageRepository.save(cafePage);
    }

    private void rejectReport(ContentReport report) {
        report.setStatus(ReportStatus.REJECTED);
        report.setResolvedAt(LocalDateTime.now());
        contentReportRepository.save(report);
    }

    private void resolveReport(ContentReport report) {
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedAt(LocalDateTime.now());
        contentReportRepository.save(report);
    }

    private void markFailed(UUID jobId, RuntimeException exception) {
        transactionTemplate.executeWithoutResult(status -> {
            AdminReportAiAutoApplyJob job = findJob(jobId);
            job.setStatus(AdminReportAiAutoApplyJobStatus.FAILED);
            job.setLastError(exception.getMessage());
            jobRepository.save(job);
            log.warn(
                    "AdminReportAiAutoApplyJob failed jobId={} reportId={} targetType={} targetAction={} reason={}",
                    job.getId(),
                    job.getContentReport() == null ? null : job.getContentReport().getId(),
                    job.getTargetType(),
                    job.getTargetAction(),
                    exception.getMessage());
        });
    }

    private void markSkipped(AdminReportAiAutoApplyJob job, String reason) {
        job.setStatus(AdminReportAiAutoApplyJobStatus.SKIPPED);
        job.setLastError(reason);
        jobRepository.save(job);
    }

    private void cancelJob(AdminReportAiAutoApplyJob job, UUID adminUserId, String reason) {
        job.setStatus(AdminReportAiAutoApplyJobStatus.CANCELLED);
        job.setCancelledAt(LocalDateTime.now());
        job.setCancelledByAdminUserId(adminUserId);
        job.setCancellationReason(reason);
    }

    private AdminReportAiAutoApplyJob findJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auto apply job not found"));
    }

    private String safetyWarning(AdminReportAiResolution resolution) {
        if (resolution.getReportDecision() == AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW) {
            return "AI recommendation needs manual review; auto apply was not scheduled.";
        }
        if (score(resolution.getConfidenceScore()) < MIN_CONFIDENCE) {
            return "AI confidence is below the auto apply threshold.";
        }
        if (resolution.getReportDecision() == AdminReportAiReportDecision.REJECT) {
            return score(resolution.getRiskScore()) <= MAX_REJECT_RISK
                    ? null
                    : "AI risk score is too high to auto reject the report.";
        }
        if (resolution.getReportDecision() == AdminReportAiReportDecision.RESOLVE) {
            if (score(resolution.getRiskScore()) < MIN_RESOLVE_RISK) {
                return "AI risk score is below the auto resolve threshold.";
            }
            if (!isResolveActionAllowed(resolution.getTargetType(), resolution.getTargetAction())) {
                return "AI target action is not safe for auto resolve.";
            }
        }
        return null;
    }

    private boolean isResolveActionAllowed(ReportTargetType targetType, AdminReportAiTargetAction action) {
        return switch (targetType) {
            case BLOG, COMMENT -> action == AdminReportAiTargetAction.HIDE
                    || action == AdminReportAiTargetAction.REMOVE;
            case USER -> action == AdminReportAiTargetAction.SUSPEND_USER;
            case CAFE_PAGE -> action == AdminReportAiTargetAction.SUSPEND_PAGE;
        };
    }

    private double score(Double value) {
        return value == null ? 0.0 : value;
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
