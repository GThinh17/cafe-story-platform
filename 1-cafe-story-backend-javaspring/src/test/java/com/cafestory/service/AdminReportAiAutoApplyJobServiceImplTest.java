package com.cafestory.service;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiAutoApplyJobResponseDTO;
import com.cafestory.entity.AdminReportAiAutoApplyJob;
import com.cafestory.entity.AdminReportAiResolution;
import com.cafestory.entity.Blog;
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
        service = new AdminReportAiAutoApplyJobServiceImpl(
                jobRepository,
                contentReportRepository,
                new NoopTransactionManager(),
                AdminReportAiAutoApplyJobServiceImpl.RECOMMENDATION_ONLY_MODE);

        when(jobRepository.save(any(AdminReportAiAutoApplyJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void scheduleIfRequested_blocksEveryAiAutoApplyRequestInA0_TC001() {
        ContentReport report = report();
        AdminReportAiResolution resolution = resolution(report);
        AdminReportAiResolutionCreateRequestDTO request = new AdminReportAiResolutionCreateRequestDTO();
        request.setAutoApplyEnabled(true);
        request.setAutoApplyDelayMinutes(15);

        AdminReportAiAutoApplyJobService.ScheduleResult result =
                service.scheduleIfRequested(report, resolution, request, UUID.randomUUID());

        assertThat(result.job()).isNull();
        assertThat(result.warning()).contains("A0_RECOMMEND_ONLY").contains("admin decision");
        verify(jobRepository, never()).save(any(AdminReportAiAutoApplyJob.class));
    }

    @Test
    void scheduleIfRequested_noRequestReturnsNoWarning_TC002() {
        AdminReportAiAutoApplyJobService.ScheduleResult missing =
                service.scheduleIfRequested(report(), resolution(report()), null, UUID.randomUUID());

        AdminReportAiResolutionCreateRequestDTO disabled = new AdminReportAiResolutionCreateRequestDTO();
        disabled.setAutoApplyEnabled(false);
        AdminReportAiAutoApplyJobService.ScheduleResult explicitlyDisabled =
                service.scheduleIfRequested(report(), resolution(report()), disabled, UUID.randomUUID());

        assertThat(missing.job()).isNull();
        assertThat(missing.warning()).isNull();
        assertThat(explicitlyDisabled.job()).isNull();
        assertThat(explicitlyDisabled.warning()).isNull();
    }

    @Test
    void processDueJobs_quarantinesLegacyJobsWithoutMutatingReportOrTarget_TC003() {
        ContentReport report = report();
        Blog blog = report.getBlog();
        AdminReportAiAutoApplyJob job = job(report);
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of(job));

        int processed = service.processDueJobs(10);

        assertThat(processed).isEqualTo(1);
        assertThat(job.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.SKIPPED);
        assertThat(job.getLastError()).contains("A0_RECOMMEND_ONLY").contains("no report or target mutation");
        assertThat(report.getStatus()).isEqualTo(ReportStatus.OPEN);
        assertThat(blog.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        verify(jobRepository).save(job);
        verify(contentReportRepository, never()).save(any(ContentReport.class));
    }

    @Test
    void processDueJobs_emptyBatchDoesNothing_TC004() {
        when(jobRepository.claimDueJobs(any(), any(), anyInt())).thenReturn(List.of());

        assertThat(service.processDueJobs(0)).isZero();
        verify(jobRepository, never()).save(any(AdminReportAiAutoApplyJob.class));
    }

    @Test
    void constructor_rejectsUnapprovedAutomationMode_TC005() {
        assertThatThrownBy(() -> new AdminReportAiAutoApplyJobServiceImpl(
                jobRepository,
                contentReportRepository,
                new NoopTransactionManager(),
                "A1_AUTO_APPLY"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only permits A0_RECOMMEND_ONLY");
    }

    @Test
    void getJobs_validatesReportAndReturnsLegacyHistory_TC006() {
        UUID reportId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 20);
        AdminReportAiAutoApplyJob job = job(report());

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
    }

    @Test
    void cancelJob_allowsScheduledLegacyJobOnly_TC007() {
        AdminReportAiAutoApplyJob scheduled = job(report());
        UUID adminId = UUID.randomUUID();
        when(jobRepository.findById(scheduled.getId())).thenReturn(Optional.of(scheduled));

        AdminReportAiAutoApplyJobResponseDTO result = service.cancelJob(scheduled.getId(), adminId);

        assertThat(result.getStatus()).isEqualTo(AdminReportAiAutoApplyJobStatus.CANCELLED);
        assertThat(scheduled.getCancelledByAdminUserId()).isEqualTo(adminId);

        scheduled.setStatus(AdminReportAiAutoApplyJobStatus.SKIPPED);
        assertThatThrownBy(() -> service.cancelJob(scheduled.getId(), adminId))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void cancelJob_rejectsNullAndMissingJob_TC008() {
        assertThatThrownBy(() -> service.cancelJob(null, UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class);

        UUID missingId = UUID.randomUUID();
        when(jobRepository.findById(missingId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cancelJob(missingId, UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class);
    }

    private ContentReport report() {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setStatus(PostStatus.PUBLISHED);

        ContentReport report = new ContentReport();
        report.setId(UUID.randomUUID());
        report.setTargetType(ReportTargetType.BLOG);
        report.setStatus(ReportStatus.OPEN);
        report.setBlog(blog);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    private AdminReportAiResolution resolution(ContentReport report) {
        AdminReportAiResolution resolution = new AdminReportAiResolution();
        resolution.setId(UUID.randomUUID());
        resolution.setContentReport(report);
        resolution.setTargetType(ReportTargetType.BLOG);
        resolution.setTargetId(report.getBlog() == null ? UUID.randomUUID() : report.getBlog().getId());
        resolution.setReportDecision(AdminReportAiReportDecision.RESOLVE);
        resolution.setTargetAction(AdminReportAiTargetAction.HIDE);
        resolution.setConfidenceScore(99.0);
        resolution.setRiskScore(99.0);
        resolution.setCreatedAt(LocalDateTime.now());
        return resolution;
    }

    private AdminReportAiAutoApplyJob job(ContentReport report) {
        AdminReportAiAutoApplyJob job = new AdminReportAiAutoApplyJob();
        job.setId(UUID.randomUUID());
        job.setContentReport(report);
        job.setAiResolution(resolution(report));
        job.setTargetType(ReportTargetType.BLOG);
        job.setTargetId(report.getBlog().getId());
        job.setReportDecision(AdminReportAiReportDecision.RESOLVE);
        job.setTargetAction(AdminReportAiTargetAction.HIDE);
        job.setConfidenceScore(99.0);
        job.setRiskScore(99.0);
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
