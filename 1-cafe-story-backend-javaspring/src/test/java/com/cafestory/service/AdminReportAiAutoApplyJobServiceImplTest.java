package com.cafestory.service;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiAutoApplyJobResponseDTO;
import com.cafestory.entity.AdminReportAiAutoApplyJob;
import com.cafestory.entity.AdminReportAiResolution;
import com.cafestory.entity.Blog;
import com.cafestory.entity.Comment;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.enums.AdminReportAiAutoApplyJobStatus;
import com.cafestory.entity.enums.AdminReportAiReportDecision;
import com.cafestory.entity.enums.AdminReportAiTargetAction;
import com.cafestory.entity.enums.PostStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.AdminReportAiAutoApplyJobRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.service.serviceImplement.AdminReportAiAutoApplyJobServiceImpl;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminReportAiAutoApplyJobServiceImplTest {

    private AdminReportAiAutoApplyJobRepository jobRepository;
    private ContentReportRepository contentReportRepository;
    private AdminReportAiAutoApplyJobServiceImpl service;

    @BeforeEach
    void setUp() {
        jobRepository = mock(AdminReportAiAutoApplyJobRepository.class);
        contentReportRepository = mock(ContentReportRepository.class);
        service = service(AdminReportAiAutoApplyJobServiceImpl.RECOMMENDATION_ONLY_MODE);

        when(jobRepository.save(any(AdminReportAiAutoApplyJob.class))).thenAnswer(invocation -> {
            AdminReportAiAutoApplyJob job = invocation.getArgument(0);
            if (job.getId() == null) {
                job.setId(UUID.randomUUID());
            }
            if (job.getCreatedAt() == null) {
                job.setCreatedAt(LocalDateTime.now());
            }
            return job;
        });
        when(contentReportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void scheduleIfRequested_blocksEveryAiAutoApplyRequestInA0_TC001() {
        ContentReport report = blogReport();
        AdminReportAiResolution resolution = eligibleResolution(report);
        AdminReportAiResolutionCreateRequestDTO request = new AdminReportAiResolutionCreateRequestDTO();
        request.setAutoApplyEnabled(true);
        request.setAutoApplyDelayMinutes(5);

        AdminReportAiAutoApplyJobService.ScheduleResult result =
                service.scheduleIfRequested(report, resolution, request, UUID.randomUUID());

        assertThat(result.job()).isNull();
        assertThat(result.warning()).contains("A0_RECOMMEND_ONLY").contains("admin decision");
        verify(jobRepository, never()).save(any(AdminReportAiAutoApplyJob.class));
    }

    @Test
    void scheduleIfRequested_noRequestReturnsNoWarningInA0_TC002() {
        AdminReportAiAutoApplyJobService.ScheduleResult missing =
                service.scheduleIfRequested(blogReport(), eligibleResolution(blogReport()), null, UUID.randomUUID());

        AdminReportAiResolutionCreateRequestDTO disabled = new AdminReportAiResolutionCreateRequestDTO();
        disabled.setAutoApplyEnabled(false);
        AdminReportAiAutoApplyJobService.ScheduleResult explicitlyDisabled =
                service.scheduleIfRequested(blogReport(), eligibleResolution(blogReport()), disabled, UUID.randomUUID());

        assertThat(missing.job()).isNull();
        assertThat(missing.warning()).isNull();
        assertThat(explicitlyDisabled.job()).isNull();
        assertThat(explicitlyDisabled.warning()).isNull();
    }

    @Test
    void scheduleIfRequested_blogEligibleInA1CreatesDelayedJob_TC003() {
        service = service(AdminReportAiAutoApplyJobServiceImpl.AUTO_HIDE_BLOG_COMMENT_MODE);
        ContentReport report = blogReport();
        AdminReportAiResolution resolution = eligibleResolution(report);

        AdminReportAiAutoApplyJobService.ScheduleResult result =
                service.scheduleIfRequested(report, resolution, null, UUID.randomUUID());

        assertThat(result.warning()).isNull();
        assertThat(result.job()).isNotNull();
        assertThat(result.job().getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.SCHEDULED);
        assertThat(result.job().getScheduledAt()).isAfter(LocalDateTime.now().plusMinutes(4));
    }

    @Test
    void processDueJobs_blogEligibleInA1ResolvesReportAndHidesBlog_TC004() {
        service = service(AdminReportAiAutoApplyJobServiceImpl.AUTO_HIDE_BLOG_COMMENT_MODE);
        ContentReport report = blogReport();
        AdminReportAiAutoApplyJob job = job(report, eligibleResolution(report));
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(job));

        int processed = service.processDueJobs(10);

        assertThat(processed).isEqualTo(1);
        assertThat(job.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.APPLIED);
        assertThat(job.getAppliedAt()).isNotNull();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(report.getResolvedAt()).isNotNull();
        assertThat(report.getBlog().getStatus()).isEqualTo(PostStatus.HIDDEN);
        verify(contentReportRepository).save(report);
    }

    @Test
    void processDueJobs_commentEligibleInA1HidesCommentOnly_TC005() {
        service = service(AdminReportAiAutoApplyJobServiceImpl.AUTO_HIDE_BLOG_COMMENT_MODE);
        ContentReport report = commentReport();
        AdminReportAiAutoApplyJob job = job(report, eligibleResolution(report));
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(job));

        int processed = service.processDueJobs(10);

        assertThat(processed).isEqualTo(1);
        assertThat(job.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.APPLIED);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(report.getComment().getStatus()).isEqualTo(PostStatus.HIDDEN);
        assertThat(report.getComment().getBlog().getStatus()).isEqualTo(PostStatus.PUBLISHED);
    }

    @Test
    void scheduleIfRequested_missingEvidenceOrBlockedReasonsDoNotCreateJob_TC006() {
        service = service(AdminReportAiAutoApplyJobServiceImpl.AUTO_HIDE_BLOG_COMMENT_MODE);
        ContentReport report = blogReport();
        AdminReportAiResolution missingEvidence = eligibleResolution(report);
        missingEvidence.setEvidenceSummary(Map.of(
                "usedEvidenceIds", List.of("EV-TARGET-CONTENT"),
                "missingEvidenceIds", List.of("EV-TARGET-MEDIA")));
        AdminReportAiResolution blocked = eligibleResolution(report);
        blocked.setBlockedReasons(List.of("CRITICAL_EVIDENCE_MISSING"));
        AdminReportAiResolution weak = eligibleResolution(report);
        weak.setEvidenceQuality("LOW");

        assertThat(service.scheduleIfRequested(report, missingEvidence, null, UUID.randomUUID()).job()).isNull();
        assertThat(service.scheduleIfRequested(report, blocked, null, UUID.randomUUID()).job()).isNull();
        assertThat(service.scheduleIfRequested(report, weak, null, UUID.randomUUID()).job()).isNull();
    }

    @Test
    void processDueJobs_skipsWhenReportResolvedOrTargetChangedBeforeDelay_TC007() {
        service = service(AdminReportAiAutoApplyJobServiceImpl.AUTO_HIDE_BLOG_COMMENT_MODE);
        ContentReport report = blogReport();
        report.setStatus(ReportStatus.RESOLVED);
        AdminReportAiAutoApplyJob resolvedReportJob = job(report, eligibleResolution(report));
        ContentReport hiddenTargetReport = blogReport();
        hiddenTargetReport.getBlog().setStatus(PostStatus.HIDDEN);
        AdminReportAiAutoApplyJob hiddenTargetJob = job(hiddenTargetReport, eligibleResolution(hiddenTargetReport));
        when(jobRepository.claimDueJobs(any(), any(), anyInt()))
                .thenReturn(List.of(resolvedReportJob, hiddenTargetJob));

        assertThat(service.processDueJobs(10)).isEqualTo(2);

        assertThat(resolvedReportJob.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.SKIPPED);
        assertThat(hiddenTargetJob.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.SKIPPED);
        verify(contentReportRepository, never()).save(any(ContentReport.class));
    }

    @Test
    void cancelJob_beforeDelayDoesNotMutate_TC008() {
        AdminReportAiAutoApplyJob scheduled = job(blogReport(), eligibleResolution(blogReport()));
        UUID adminId = UUID.randomUUID();
        when(jobRepository.findById(scheduled.getId())).thenReturn(Optional.of(scheduled));

        AdminReportAiAutoApplyJobResponseDTO result = service.cancelJob(scheduled.getId(), adminId);

        assertThat(result.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.CANCELLED);
        assertThat(scheduled.getCancelledByAdminUserId()).isEqualTo(adminId);
        verify(contentReportRepository, never()).save(any(ContentReport.class));

        scheduled.setStatus(AdminReportAiAutoApplyJobStatus.SKIPPED);
        assertThatThrownBy(() -> service.cancelJob(scheduled.getId(), adminId))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void processDueJobs_a0StillQuarantinesLegacyJobsWithoutMutation_TC009() {
        ContentReport report = blogReport();
        Blog blog = report.getBlog();
        AdminReportAiAutoApplyJob job = job(report, eligibleResolution(report));
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(job));

        int processed = service.processDueJobs(10);

        assertThat(processed).isEqualTo(1);
        assertThat(job.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.SKIPPED);
        assertThat(job.getLastError()).contains("A0_RECOMMEND_ONLY").contains("no report or target mutation");
        assertThat(report.getStatus()).isEqualTo(ReportStatus.OPEN);
        assertThat(blog.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        verify(contentReportRepository, never()).save(any(ContentReport.class));
    }

    @Test
    void getJobsAndConstructorValidationRemainFailClosed_TC010() {
        UUID reportId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 20);
        AdminReportAiAutoApplyJob job = job(blogReport(), eligibleResolution(blogReport()));

        assertThatThrownBy(() -> service.getJobs(null, pageable))
                .isInstanceOf(ResponseStatusException.class);
        when(contentReportRepository.existsById(reportId)).thenReturn(false);
        assertThatThrownBy(() -> service.getJobs(reportId, pageable))
                .isInstanceOf(ResponseStatusException.class);

        when(contentReportRepository.existsById(reportId)).thenReturn(true);
        when(jobRepository.findByContentReportId(reportId, pageable))
                .thenReturn(new PageImpl<>(List.of(job)));
        assertThat(service.getJobs(reportId, pageable).getContent())
                .singleElement()
                .extracting(AdminReportAiAutoApplyJobResponseDTO::getStatus)
                .isEqualTo(AdminReportAiAutoApplyJobStatus.SCHEDULED);

        assertThatThrownBy(() -> service("A1_AUTO_APPLY"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unsupported admin.report.ai.automation-mode");
    }

    @Test
    void cancelJob_rejectsNullAndMissingJob_TC011() {
        assertThatThrownBy(() -> service.cancelJob(null, UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class);

        UUID missingId = UUID.randomUUID();
        when(jobRepository.findById(missingId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cancelJob(missingId, UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class);
    }

    private AdminReportAiAutoApplyJobServiceImpl service(String mode) {
        return new AdminReportAiAutoApplyJobServiceImpl(
                jobRepository,
                contentReportRepository,
                new NoopTransactionManager(),
                mode,
                5);
    }

    private ContentReport blogReport() {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setStatus(PostStatus.PUBLISHED);
        blog.setContent("Reported blog content asks for bank OTP and transfer money through a phishing link.");
        blog.setCreatedAt(LocalDateTime.now().minusHours(2));

        ContentReport report = new ContentReport();
        report.setId(UUID.randomUUID());
        report.setTargetType(ReportTargetType.BLOG);
        report.setStatus(ReportStatus.OPEN);
        report.setBlog(blog);
        report.setCreatedAt(LocalDateTime.now().minusHours(1));
        return report;
    }

    private ContentReport commentReport() {
        Blog parentBlog = new Blog();
        parentBlog.setId(UUID.randomUUID());
        parentBlog.setStatus(PostStatus.PUBLISHED);
        parentBlog.setContent("Parent blog content");
        parentBlog.setCreatedAt(LocalDateTime.now().minusHours(3));

        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setBlog(parentBlog);
        comment.setStatus(PostStatus.PUBLISHED);
        comment.setContent("Reported comment asks for bank OTP and transfer money through a phishing link.");
        comment.setCreatedAt(LocalDateTime.now().minusHours(2));

        ContentReport report = new ContentReport();
        report.setId(UUID.randomUUID());
        report.setTargetType(ReportTargetType.COMMENT);
        report.setStatus(ReportStatus.OPEN);
        report.setComment(comment);
        report.setCreatedAt(LocalDateTime.now().minusHours(1));
        return report;
    }

    private AdminReportAiResolutionCreateRequestDTO autoApplyRequest(int delayMinutes) {
        AdminReportAiResolutionCreateRequestDTO request = new AdminReportAiResolutionCreateRequestDTO();
        request.setAutoApplyEnabled(true);
        request.setAutoApplyDelayMinutes(delayMinutes);
        return request;
    }

    private AdminReportAiAutoApplyJob job(
            ContentReport report,
            AdminReportAiReportDecision decision,
            AdminReportAiTargetAction action) {
        AdminReportAiAutoApplyJob job = new AdminReportAiAutoApplyJob();
        job.setId(UUID.randomUUID());
        job.setContentReport(report);
        job.setAiResolution(resolution(report, decision, action, 90.0, 80.0));
        job.setTargetType(report.getTargetType());
        job.setTargetId(UUID.randomUUID());
        job.setReportDecision(decision);
        job.setTargetAction(action);
        job.setConfidenceScore(90.0);
        job.setRiskScore(80.0);
        job.setStatus(AdminReportAiAutoApplyJobStatus.SCHEDULED);
        job.setScheduledAt(LocalDateTime.now().minusSeconds(1));
        job.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        return job;
    }

    @Test
    void scheduleIfRequested_skip_nullRequestOrDisabled_TC012() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiResolution resolution = resolution(
                report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE, 95.0, 10.0);

        assertThat(service.scheduleIfRequested(report, resolution, null, null).job()).isNull();

        AdminReportAiResolutionCreateRequestDTO disabled = new AdminReportAiResolutionCreateRequestDTO();
        disabled.setAutoApplyEnabled(false);
        assertThat(service.scheduleIfRequested(report, resolution, disabled, null).warning()).isNull();

        verify(jobRepository, never()).save(any(AdminReportAiAutoApplyJob.class));
    }

    @Test
    void scheduleIfRequested_skip_delayMinutesMissing_TC013() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiResolution resolution = resolution(
                report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE, 95.0, 80.0);
        AdminReportAiResolutionCreateRequestDTO request = new AdminReportAiResolutionCreateRequestDTO();
        request.setAutoApplyEnabled(true);

        var result = service.scheduleIfRequested(report, resolution, request, null);

        assertThat(result.job()).isNull();
        assertThat(result.warning()).isEqualTo("Auto apply delay is required.");
    }

    @Test
    void scheduleIfRequested_skip_anotherJobIsAlreadyApplying_TC014() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiResolution resolution = resolution(
                report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE, 95.0, 80.0);
        AdminReportAiResolutionCreateRequestDTO request = new AdminReportAiResolutionCreateRequestDTO();
        request.setAutoApplyEnabled(true);
        request.setAutoApplyDelayMinutes(30);
        AdminReportAiAutoApplyJob applying = job(
                report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE);
        applying.setStatus(AdminReportAiAutoApplyJobStatus.APPLYING);
        when(jobRepository.findFirstByContentReportIdAndStatusInOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.of(applying));

        var result = service.scheduleIfRequested(report, resolution, request, null);

        assertThat(result.job()).isNull();
        assertThat(result.warning()).isEqualTo("An auto apply job is already applying for this report.");
    }

    @Test
    void getJobs_success_listsJobsOfReport_TC015() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiAutoApplyJob job = job(
                report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE);
        org.springframework.data.domain.PageRequest pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        when(contentReportRepository.existsById(report.getId())).thenReturn(true);
        when(jobRepository.findByContentReportId(report.getId(), pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(job)));

        var result = service.getJobs(report.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(job.getId());
        assertThat(result.getContent().get(0).getAiResolutionId())
                .isEqualTo(job.getAiResolution().getId());
    }

    @Test
    void getJobs_success_jobWithoutResolutionMapsNullId_TC016() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiAutoApplyJob job = job(
                report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE);
        job.setAiResolution(null);
        org.springframework.data.domain.PageRequest pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        when(contentReportRepository.existsById(report.getId())).thenReturn(true);
        when(jobRepository.findByContentReportId(report.getId(), pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(job)));

        assertThat(service.getJobs(report.getId(), pageable).getContent().get(0).getAiResolutionId())
                .isNull();
    }

    @Test
    void getJobs_fail_reportIdMissingOrUnknown_TC017() {
        org.springframework.data.domain.PageRequest pageable =
                org.springframework.data.domain.PageRequest.of(0, 10);
        UUID unknownReportId = UUID.randomUUID();
        when(contentReportRepository.existsById(unknownReportId)).thenReturn(false);

        assertThatThrownBy(() -> service.getJobs(null, pageable))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Report id is required");
        assertThatThrownBy(() -> service.getJobs(unknownReportId, pageable))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Report not found");
    }

    @Test
    void cancelJob_fail_jobIdMissing_TC018() {
        UUID adminUserId = UUID.randomUUID();

        assertThatThrownBy(() -> service.cancelJob(null, adminUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Auto apply job id is required");
    }

    @Test
    void processDueJobs_success_commentHideAndRemove_TC019() {
        ContentReport hideReport = report(ReportTargetType.COMMENT);
        Comment hidden = new Comment();
        hidden.setId(UUID.randomUUID());
        hidden.setStatus(PostStatus.PUBLISHED);
        hideReport.setComment(hidden);
        AdminReportAiAutoApplyJob hideJob = job(
                hideReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE);

        ContentReport removeReport = report(ReportTargetType.COMMENT);
        Comment removed = new Comment();
        removed.setId(UUID.randomUUID());
        removed.setStatus(PostStatus.PUBLISHED);
        removeReport.setComment(removed);
        AdminReportAiAutoApplyJob removeJob = job(
                removeReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.REMOVE);

        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(hideJob, removeJob));
        when(jobRepository.findById(hideJob.getId())).thenReturn(Optional.of(hideJob));
        when(jobRepository.findById(removeJob.getId())).thenReturn(Optional.of(removeJob));

        assertThat(service.processDueJobs(10)).isEqualTo(2);
        assertThat(hidden.getStatus()).isEqualTo(PostStatus.HIDDEN);
        assertThat(removed.getStatus()).isEqualTo(PostStatus.REMOVED);
    }

    @Test
    void processDueJobs_success_blogRemoveAction_TC020() {
        ContentReport report = report(ReportTargetType.BLOG);
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setStatus(PostStatus.PUBLISHED);
        report.setBlog(blog);
        AdminReportAiAutoApplyJob job = job(
                report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.REMOVE);
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(job));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        service.processDueJobs(10);

        assertThat(blog.getStatus()).isEqualTo(PostStatus.REMOVED);
    }

    @Test
    void processDueJobs_fail_targetMissingMarksJobFailed_TC021() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiAutoApplyJob job = job(
                report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE);
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(job));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        assertThat(service.processDueJobs(10)).isEqualTo(1);

        assertThat(job.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.FAILED);
        assertThat(job.getLastError()).contains("Report has no blog target");
    }

    @Test
    void processDueJobs_fail_actionNotAllowedForTarget_TC022() {
        ContentReport blogReport = report(ReportTargetType.BLOG);
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setStatus(PostStatus.PUBLISHED);
        blogReport.setBlog(blog);
        AdminReportAiAutoApplyJob blogJob = job(
                blogReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.APPROVE);

        ContentReport userReport = report(ReportTargetType.USER);
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setAccountStatus(true);
        userReport.setReportedUser(user);
        AdminReportAiAutoApplyJob userJob = job(
                userReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.KEEP_ACTIVE);

        ContentReport pageReport = report(ReportTargetType.CAFE_PAGE);
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        cafePage.setStatus(PageStatus.ACTIVE);
        pageReport.setCafePage(cafePage);
        AdminReportAiAutoApplyJob pageJob = job(
                pageReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.KEEP_ACTIVE);

        when(jobRepository.claimDueJobs(any(), any(), anyInt()))
                .thenReturn(List.of(blogJob, userJob, pageJob));
        when(jobRepository.findById(blogJob.getId())).thenReturn(Optional.of(blogJob));
        when(jobRepository.findById(userJob.getId())).thenReturn(Optional.of(userJob));
        when(jobRepository.findById(pageJob.getId())).thenReturn(Optional.of(pageJob));

        assertThat(service.processDueJobs(10)).isEqualTo(3);

        assertThat(blogJob.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.FAILED);
        assertThat(userJob.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.FAILED);
        assertThat(pageJob.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.FAILED);
        assertThat(userJob.getLastError()).contains("Auto apply action is not");
    }

    @Test
    void processDueJobs_fail_missingUserOrCafePageTarget_TC023() {
        ContentReport userReport = report(ReportTargetType.USER);
        AdminReportAiAutoApplyJob userJob = job(
                userReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.SUSPEND_USER);
        ContentReport pageReport = report(ReportTargetType.CAFE_PAGE);
        AdminReportAiAutoApplyJob pageJob = job(
                pageReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.SUSPEND_PAGE);
        ContentReport commentReport = report(ReportTargetType.COMMENT);
        AdminReportAiAutoApplyJob commentJob = job(
                commentReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE);

        when(jobRepository.claimDueJobs(any(), any(), anyInt()))
                .thenReturn(List.of(userJob, pageJob, commentJob));
        when(jobRepository.findById(userJob.getId())).thenReturn(Optional.of(userJob));
        when(jobRepository.findById(pageJob.getId())).thenReturn(Optional.of(pageJob));
        when(jobRepository.findById(commentJob.getId())).thenReturn(Optional.of(commentJob));

        service.processDueJobs(10);

        assertThat(userJob.getLastError()).contains("Report has no user target");
        assertThat(pageJob.getLastError()).contains("Report has no cafe page target");
        assertThat(commentJob.getLastError()).contains("Report has no comment target");
    }

    @Test
    void processDueJobs_success_missingReportIsSkipped_TC024() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiAutoApplyJob job = job(
                report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE);
        job.setContentReport(null);
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(job));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        service.processDueJobs(0);

        assertThat(job.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.SKIPPED);
    }

    @Test
    void scheduleIfRequested_skip_rejectDecisionWithHighRisk_TC025() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiResolution resolution = resolution(
                report, AdminReportAiReportDecision.REJECT, AdminReportAiTargetAction.APPROVE, 95.0, 90.0);

        var result = service.scheduleIfRequested(report, resolution, autoApplyRequest(15), UUID.randomUUID());

        assertThat(result.job()).isNull();
        assertThat(result.warning()).contains("risk score is too high");
    }

    @Test
    void scheduleIfRequested_success_rejectDecisionWithLowRisk_TC026() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiResolution resolution = resolution(
                report, AdminReportAiReportDecision.REJECT, AdminReportAiTargetAction.APPROVE, 95.0, 5.0);

        var result = service.scheduleIfRequested(report, resolution, autoApplyRequest(15), UUID.randomUUID());

        assertThat(result.warning()).isNull();
        assertThat(result.job()).isNotNull();
    }

    @Test
    void scheduleIfRequested_skip_resolveActionNotSafeForTargetType_TC027() {
        ContentReport blogReport = report(ReportTargetType.BLOG);
        AdminReportAiResolution approveOnBlog = resolution(
                blogReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.APPROVE, 95.0, 90.0);
        ContentReport userReport = report(ReportTargetType.USER);
        AdminReportAiResolution keepUser = resolution(
                userReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.KEEP_ACTIVE, 95.0, 90.0);
        ContentReport pageReport = report(ReportTargetType.CAFE_PAGE);
        AdminReportAiResolution keepPage = resolution(
                pageReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.KEEP_ACTIVE, 95.0, 90.0);

        assertThat(service.scheduleIfRequested(blogReport, approveOnBlog, autoApplyRequest(15), null).warning())
                .contains("not safe for auto resolve");
        assertThat(service.scheduleIfRequested(userReport, keepUser, autoApplyRequest(15), null).warning())
                .contains("not safe for auto resolve");
        assertThat(service.scheduleIfRequested(pageReport, keepPage, autoApplyRequest(15), null).warning())
                .contains("not safe for auto resolve");
    }

    @Test
    void scheduleIfRequested_success_suspendActionsAreSafeForUserAndCafePage_TC028() {
        ContentReport userReport = report(ReportTargetType.USER);
        AdminReportAiResolution suspendUser = resolution(
                userReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.SUSPEND_USER, 95.0, 90.0);
        ContentReport pageReport = report(ReportTargetType.CAFE_PAGE);
        AdminReportAiResolution suspendPage = resolution(
                pageReport, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.SUSPEND_PAGE, 95.0, 90.0);

        assertThat(service.scheduleIfRequested(userReport, suspendUser, autoApplyRequest(15), null).job())
                .isNotNull();
        assertThat(service.scheduleIfRequested(pageReport, suspendPage, autoApplyRequest(15), null).job())
                .isNotNull();
    }

    @Test
    void scheduleIfRequested_skip_lowRiskResolveIsNotAutoApplied_TC029() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiResolution resolution = resolution(
                report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE, 95.0, 5.0);

        assertThat(service.scheduleIfRequested(report, resolution, autoApplyRequest(15), null).warning())
                .contains("risk score is below the auto resolve threshold");
    }

    private AdminReportAiResolution resolution(
            ContentReport report,
            AdminReportAiReportDecision decision,
            AdminReportAiTargetAction action,
            double confidenceScore,
            double riskScore) {
        AdminReportAiResolution resolution = new AdminReportAiResolution();
        resolution.setId(UUID.randomUUID());
        resolution.setContentReport(report);
        resolution.setTargetType(report.getTargetType());
        resolution.setTargetId(report.getTargetType() == ReportTargetType.BLOG
                ? report.getBlog().getId()
                : report.getComment().getId());
        resolution.setReportDecision(AdminReportAiReportDecision.RESOLVE);
        resolution.setTargetAction(AdminReportAiTargetAction.HIDE);
        resolution.setEvidenceQuality("HIGH");
        resolution.setEvidenceSufficiency("SUFFICIENT");
        resolution.setViolationLikelihood("HIGH");
        resolution.setBlockedReasons(List.of());
        resolution.setEvidenceSummary(Map.of(
                "usedEvidenceIds", List.of("EV-TARGET-CONTENT"),
                "counterEvidenceIds", List.of(),
                "missingEvidenceIds", List.of()));
        resolution.setFindings(List.of(new java.util.LinkedHashMap<>(Map.of(
                "ruleId", "CSR.SPAM.001",
                "ruleVersion", "1.0.0-proposed.2",
                "outcome", "SUBSTANTIATED",
                "evidenceIds", List.of("EV-TARGET-CONTENT"),
                "counterEvidenceIds", List.of(),
                "missingEvidenceIds", List.of()))));
        resolution.setCreatedAt(LocalDateTime.now());
        return resolution;
    }

    private AdminReportAiAutoApplyJob job(ContentReport report, AdminReportAiResolution resolution) {
        AdminReportAiAutoApplyJob job = new AdminReportAiAutoApplyJob();
        job.setId(UUID.randomUUID());
        job.setContentReport(report);
        job.setAiResolution(resolution);
        job.setTargetType(resolution.getTargetType());
        job.setTargetId(resolution.getTargetId());
        job.setReportDecision(resolution.getReportDecision());
        job.setTargetAction(resolution.getTargetAction());
        job.setStatus(AdminReportAiAutoApplyJobStatus.SCHEDULED);
        job.setScheduledAt(LocalDateTime.now().minusMinutes(1));
        job.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        return job;
    }

    private static class NoopTransactionManager implements PlatformTransactionManager {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
        }

        @Override
        public void rollback(TransactionStatus status) {
        }
    }
}
