package com.cafestory.service;

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
import com.cafestory.service.serviceImplement.AdminReportAiAutoApplyJobServiceImpl;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
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
    private BlogRepository blogRepository;
    private CommentRepository commentRepository;
    private UserRepository userRepository;
    private CafePageRepository cafePageRepository;
    private AdminReportAiAutoApplyJobServiceImpl service;

    @BeforeEach
    void setUp() {
        jobRepository = mock(AdminReportAiAutoApplyJobRepository.class);
        contentReportRepository = mock(ContentReportRepository.class);
        blogRepository = mock(BlogRepository.class);
        commentRepository = mock(CommentRepository.class);
        userRepository = mock(UserRepository.class);
        cafePageRepository = mock(CafePageRepository.class);
        service = new AdminReportAiAutoApplyJobServiceImpl(
                jobRepository,
                contentReportRepository,
                blogRepository,
                commentRepository,
                userRepository,
                cafePageRepository,
                new NoopTransactionManager());

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
    }

    @Test
    void scheduleIfRequested_success_createsScheduledJob_TC001() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiResolution resolution = resolution(
                report,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE,
                92.0,
                84.0);
        AdminReportAiResolutionCreateRequestDTO request = autoApplyRequest(15);

        AdminReportAiAutoApplyJobService.ScheduleResult result =
                service.scheduleIfRequested(report, resolution, request, UUID.randomUUID());

        assertThat(result.warning()).isNull();
        assertThat(result.job()).isNotNull();
        assertThat(result.job().getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.SCHEDULED);
        assertThat(result.job().getScheduledAt()).isAfter(LocalDateTime.now().plusMinutes(14));
        verify(jobRepository).save(any(AdminReportAiAutoApplyJob.class));
    }

    @Test
    void scheduleIfRequested_skip_needsManualReview_TC002() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiResolution resolution = resolution(
                report,
                AdminReportAiReportDecision.NEEDS_MANUAL_REVIEW,
                AdminReportAiTargetAction.NONE,
                94.0,
                88.0);

        AdminReportAiAutoApplyJobService.ScheduleResult result =
                service.scheduleIfRequested(report, resolution, autoApplyRequest(15), UUID.randomUUID());

        assertThat(result.job()).isNull();
        assertThat(result.warning()).contains("manual review");
        verify(jobRepository, never()).save(any(AdminReportAiAutoApplyJob.class));
    }

    @Test
    void scheduleIfRequested_skip_lowConfidence_TC003() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiResolution resolution = resolution(
                report,
                AdminReportAiReportDecision.RESOLVE,
                AdminReportAiTargetAction.HIDE,
                72.0,
                88.0);

        AdminReportAiAutoApplyJobService.ScheduleResult result =
                service.scheduleIfRequested(report, resolution, autoApplyRequest(15), UUID.randomUUID());

        assertThat(result.job()).isNull();
        assertThat(result.warning()).contains("confidence");
        verify(jobRepository, never()).save(any(AdminReportAiAutoApplyJob.class));
    }

    @Test
    void scheduleIfRequested_success_cancelsExistingScheduledJob_TC004() {
        ContentReport report = report(ReportTargetType.BLOG);
        AdminReportAiAutoApplyJob existing = job(report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE);
        when(jobRepository.findFirstByContentReportIdAndStatusInOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.of(existing));

        service.scheduleIfRequested(
                report,
                resolution(report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE, 90.0, 80.0),
                autoApplyRequest(15),
                UUID.randomUUID());

        assertThat(existing.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.CANCELLED);
        assertThat(existing.getCancellationReason()).isEqualTo("REPLACED_BY_NEWER_RECOMMENDATION");
    }

    @Test
    void cancelJob_success_scheduledOnly_TC005() {
        AdminReportAiAutoApplyJob job = job(report(ReportTargetType.BLOG), AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE);
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        AdminReportAiAutoApplyJobResponseDTO result = service.cancelJob(job.getId(), UUID.randomUUID());

        assertThat(result.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.CANCELLED);
        assertThat(result.getCancelledAt()).isNotNull();
    }

    @Test
    void cancelJob_fail_appliedCannotCancel_TC006() {
        AdminReportAiAutoApplyJob job = job(report(ReportTargetType.BLOG), AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE);
        job.setStatus(AdminReportAiAutoApplyJobStatus.APPLIED);
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> service.cancelJob(job.getId(), UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void processDueJobs_success_blogHideResolvesReport_TC007() {
        ContentReport report = report(ReportTargetType.BLOG);
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setStatus(PostStatus.PUBLISHED);
        report.setBlog(blog);
        AdminReportAiAutoApplyJob job = job(report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE);
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(job));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        int processed = service.processDueJobs(10);

        assertThat(processed).isEqualTo(1);
        assertThat(blog.getStatus()).isEqualTo(PostStatus.HIDDEN);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(job.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.APPLIED);
        verify(blogRepository).save(blog);
        verify(contentReportRepository).save(report);
    }

    @Test
    void processDueJobs_success_userSuspendResolvesReport_TC008() {
        ContentReport report = report(ReportTargetType.USER);
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setAccountStatus(true);
        report.setReportedUser(user);
        AdminReportAiAutoApplyJob job = job(report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.SUSPEND_USER);
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(job));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        service.processDueJobs(10);

        assertThat(user.getAccountStatus()).isFalse();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        verify(userRepository).save(user);
    }

    @Test
    void processDueJobs_success_cafePageSuspendResolvesReport_TC009() {
        ContentReport report = report(ReportTargetType.CAFE_PAGE);
        CafePage page = new CafePage();
        page.setId(UUID.randomUUID());
        page.setStatus(PageStatus.ACTIVE);
        page.setPageActive(true);
        report.setCafePage(page);
        AdminReportAiAutoApplyJob job = job(report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.SUSPEND_PAGE);
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(job));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        service.processDueJobs(10);

        assertThat(page.getStatus()).isEqualTo(PageStatus.SUSPENDED);
        assertThat(page.getPageActive()).isFalse();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        verify(cafePageRepository).save(page);
    }

    @Test
    void processDueJobs_success_rejectReportOnly_TC010() {
        ContentReport report = report(ReportTargetType.COMMENT);
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setStatus(PostStatus.PUBLISHED);
        report.setComment(comment);
        AdminReportAiAutoApplyJob job = job(report, AdminReportAiReportDecision.REJECT, AdminReportAiTargetAction.APPROVE);
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(job));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        service.processDueJobs(10);

        assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(comment.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void processDueJobs_skip_reportAlreadyClosed_TC011() {
        ContentReport report = report(ReportTargetType.BLOG);
        report.setStatus(ReportStatus.RESOLVED);
        AdminReportAiAutoApplyJob job = job(report, AdminReportAiReportDecision.RESOLVE, AdminReportAiTargetAction.HIDE);
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(job));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        service.processDueJobs(10);

        assertThat(job.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.SKIPPED);
        assertThat(job.getLastError()).contains("no longer open");
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
        resolution.setTargetId(UUID.randomUUID());
        resolution.setReportDecision(decision);
        resolution.setTargetAction(action);
        resolution.setConfidenceScore(confidenceScore);
        resolution.setRiskScore(riskScore);
        resolution.setCreatedAt(LocalDateTime.now());
        return resolution;
    }

    private ContentReport report(ReportTargetType targetType) {
        ContentReport report = new ContentReport();
        report.setId(UUID.randomUUID());
        report.setTargetType(targetType);
        report.setStatus(ReportStatus.OPEN);
        report.setCreatedAt(LocalDateTime.now());
        return report;
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
