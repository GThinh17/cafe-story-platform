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
