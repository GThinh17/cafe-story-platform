package com.cafestory.service;

import com.cafestory.dto.requestDTO.ReportModerationRequestDTO;
import com.cafestory.dto.responseDTO.ReportModerationJobResponseDTO;
import com.cafestory.dto.responseDTO.ReportModerationResponseDTO;
import com.cafestory.entity.AiModerationResult;
import com.cafestory.entity.Blog;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Comment;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.ReportModerationJob;
import com.cafestory.entity.ReportReason;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.ReportModerationJobStatus;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.AiModerationResultRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.repository.ReportModerationJobRepository;
import com.cafestory.service.serviceImplement.ReportModerationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportModerationServiceImplTest {

    private ReportModerationJobRepository jobRepository;
    private AiModerationResultRepository moderationResultRepository;
    private ContentReportRepository contentReportRepository;

    @BeforeEach
    void setUp() {
        jobRepository = mock(ReportModerationJobRepository.class);
        moderationResultRepository = mock(AiModerationResultRepository.class);
        contentReportRepository = mock(ContentReportRepository.class);
    }

    @Test
    void enqueueReport_success_blogReportCreatesPendingJob_TC001() {
        ContentReport report = blogReport();
        when(jobRepository.existsByContentReportId(report.getId())).thenReturn(false);
        when(contentReportRepository.countByBlogIdAndStatusIn(eq(report.getBlog().getId()), anyCollection()))
                .thenReturn(2L);

        serviceReturning(response(ModerationDecision.SAFE, 10.0)).enqueueReport(report);

        ArgumentCaptor<ReportModerationJob> jobCaptor = ArgumentCaptor.forClass(ReportModerationJob.class);
        verify(jobRepository).save(jobCaptor.capture());
        ReportModerationJob job = jobCaptor.getValue();
        assertThat(job.getContentReport()).isEqualTo(report);
        assertThat(job.getTargetType()).isEqualTo(ReportTargetType.BLOG);
        assertThat(job.getTargetId()).isEqualTo(report.getBlog().getId());
        assertThat(job.getStatus()).isEqualTo(ReportModerationJobStatus.PENDING);
        assertThat(job.getAttemptCount()).isZero();
        assertThat(job.getMaxAttempts()).isEqualTo(3);
        assertThat(job.getPriorityScore()).isGreaterThan(job.getRiskScore());
    }

    @Test
    void enqueueReport_duplicateOrUnsupportedDoesNotCreateJob_TC002() {
        ContentReport blogReport = blogReport();
        when(jobRepository.existsByContentReportId(blogReport.getId())).thenReturn(true);
        ReportModerationServiceImpl service = serviceReturning(response(ModerationDecision.SAFE, 0.0));

        service.enqueueReport(blogReport);
        service.enqueueReport(userReport());
        service.enqueueReport(cafePageReport());

        verify(jobRepository, never()).save(any(ReportModerationJob.class));
    }

    @Test
    void processDueJobs_successViolationPersistsResultAndMarksReportReviewing_TC003() {
        ContentReport report = blogReport();
        ReportModerationJob job = job(report);
        when(jobRepository.claimDueJobs(any(Collection.class), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), eq(10)))
                .thenReturn(List.of(job));
        when(jobRepository.save(any(ReportModerationJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(moderationResultRepository.existsByContentReportId(report.getId())).thenReturn(false);
        when(contentReportRepository.countByBlogIdAndStatusIn(eq(report.getBlog().getId()), anyCollection()))
                .thenReturn(3L);

        int processed = serviceReturning(response(ModerationDecision.VIOLATION, 91.4)).processDueJobs(10);

        assertThat(processed).isEqualTo(1);
        assertThat(job.getAttemptCount()).isEqualTo(1);
        assertThat(job.getStatus()).isEqualTo(ReportModerationJobStatus.SUCCEEDED);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.REVIEWING);
        verify(contentReportRepository).save(report);

        ArgumentCaptor<AiModerationResult> resultCaptor = ArgumentCaptor.forClass(AiModerationResult.class);
        verify(moderationResultRepository).save(resultCaptor.capture());
        AiModerationResult savedResult = resultCaptor.getValue();
        assertThat(savedResult.getContentReport()).isEqualTo(report);
        assertThat(savedResult.getBlog()).isEqualTo(report.getBlog());
        assertThat(savedResult.getDecision()).isEqualTo(ModerationDecision.VIOLATION);
        assertThat(savedResult.getScore()).isEqualTo(91.4);
        assertThat(savedResult.getResolved()).isFalse();
        assertThat(savedResult.getPriorityScore()).isGreaterThan(91.4);
    }

    @Test
    void processDueJobs_successSafePersistsResolvedResultAndKeepsReportOpen_TC004() {
        ContentReport report = commentReport();
        ReportModerationJob job = job(report);
        when(jobRepository.claimDueJobs(any(Collection.class), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of(job));
        when(jobRepository.save(any(ReportModerationJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(moderationResultRepository.existsByContentReportId(report.getId())).thenReturn(false);

        int processed = serviceReturning(response(ModerationDecision.SAFE, 5.0)).processDueJobs(1);

        assertThat(processed).isEqualTo(1);
        assertThat(job.getStatus()).isEqualTo(ReportModerationJobStatus.SUCCEEDED);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.OPEN);
        verify(contentReportRepository, never()).save(report);
        ArgumentCaptor<AiModerationResult> resultCaptor = ArgumentCaptor.forClass(AiModerationResult.class);
        verify(moderationResultRepository).save(resultCaptor.capture());
        assertThat(resultCaptor.getValue().getResolved()).isTrue();
        assertThat(resultCaptor.getValue().getComment()).isEqualTo(report.getComment());
    }

    @Test
    void processDueJobs_failureSchedulesRetryWithBackoff_TC005() {
        ContentReport report = blogReport();
        ReportModerationJob job = job(report);
        when(jobRepository.claimDueJobs(any(Collection.class), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of(job));
        when(jobRepository.save(any(ReportModerationJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        int processed = serviceThrowing().processDueJobs(10);

        assertThat(processed).isEqualTo(1);
        assertThat(job.getAttemptCount()).isEqualTo(1);
        assertThat(job.getStatus()).isEqualTo(ReportModerationJobStatus.FAILED);
        assertThat(job.getNextAttemptAt()).isAfter(LocalDateTime.now().plusSeconds(20));
        assertThat(job.getLastError()).contains("timeout");
        verify(moderationResultRepository, never()).save(any(AiModerationResult.class));
    }

    @Test
    void processDueJobs_failureAfterMaxAttemptsMovesToDeadLetter_TC006() {
        ContentReport report = blogReport();
        ReportModerationJob job = job(report);
        job.setAttemptCount(2);
        when(jobRepository.claimDueJobs(any(Collection.class), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of(job));
        when(jobRepository.save(any(ReportModerationJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        int processed = serviceThrowing().processDueJobs(10);

        assertThat(processed).isEqualTo(1);
        assertThat(job.getAttemptCount()).isEqualTo(3);
        assertThat(job.getStatus()).isEqualTo(ReportModerationJobStatus.DEAD_LETTER);
    }

    @Test
    void processDueJobs_existingResultDoesNotCreateDuplicate_TC007() {
        ContentReport report = blogReport();
        ReportModerationJob job = job(report);
        when(jobRepository.claimDueJobs(any(Collection.class), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of(job));
        when(jobRepository.save(any(ReportModerationJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(moderationResultRepository.existsByContentReportId(report.getId())).thenReturn(true);

        int processed = serviceReturning(response(ModerationDecision.NEEDS_REVIEW, 70.0)).processDueJobs(10);

        assertThat(processed).isEqualTo(1);
        assertThat(job.getStatus()).isEqualTo(ReportModerationJobStatus.SUCCEEDED);
        verify(moderationResultRepository, never()).save(any(AiModerationResult.class));
    }

    @Test
    void retryReport_success_failedJobResetToPending_TC008() {
        ContentReport report = blogReport();
        ReportModerationJob job = job(report);
        job.setStatus(ReportModerationJobStatus.DEAD_LETTER);
        job.setLastError("timeout");
        when(jobRepository.findByContentReportId(report.getId())).thenReturn(Optional.of(job));
        when(jobRepository.save(job)).thenReturn(job);

        ReportModerationJobResponseDTO response = serviceReturning(response(ModerationDecision.SAFE, 0.0))
                .retryReport(report.getId());

        assertThat(response.getStatus()).isEqualTo(ReportModerationJobStatus.PENDING);
        assertThat(job.getStatus()).isEqualTo(ReportModerationJobStatus.PENDING);
        assertThat(job.getAttemptCount()).isZero();
        assertThat(job.getLastError()).isNull();
    }

    private ReportModerationServiceImpl serviceReturning(ReportModerationResponseDTO response) {
        return new ReportModerationServiceImpl(
                jobRepository,
                moderationResultRepository,
                contentReportRepository,
                new ObjectMapper(),
                transactionManager(),
                "http://localhost",
                1000) {
            @Override
            protected ReportModerationResponseDTO callModerationWebhook(ReportModerationRequestDTO request) {
                return response;
            }
        };
    }

    private ReportModerationServiceImpl serviceThrowing() {
        return new ReportModerationServiceImpl(
                jobRepository,
                moderationResultRepository,
                contentReportRepository,
                new ObjectMapper(),
                transactionManager(),
                "http://localhost",
                1000) {
            @Override
            protected ReportModerationResponseDTO callModerationWebhook(ReportModerationRequestDTO request) {
                throw new IllegalStateException("timeout");
            }
        };
    }

    private PlatformTransactionManager transactionManager() {
        return new AbstractPlatformTransactionManager() {
            @Override
            protected Object doGetTransaction() {
                return new Object();
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) {
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) {
            }
        };
    }

    private ReportModerationJob job(ContentReport report) {
        ReportModerationJob job = new ReportModerationJob();
        job.setId(UUID.randomUUID());
        job.setContentReport(report);
        job.setTargetType(report.getTargetType());
        job.setTargetId(report.getTargetType() == ReportTargetType.BLOG
                ? report.getBlog().getId()
                : report.getComment().getId());
        job.setStatus(ReportModerationJobStatus.PENDING);
        job.setAttemptCount(0);
        job.setMaxAttempts(3);
        job.setNextAttemptAt(LocalDateTime.now().minusSeconds(1));
        job.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        return job;
    }

    private ReportModerationResponseDTO response(ModerationDecision decision, double score) {
        ReportModerationResponseDTO response = new ReportModerationResponseDTO();
        response.setDecision(decision);
        response.setScore(score);
        response.setLabels(List.of("violence", "harassment"));
        response.setExplanation("OpenAI moderation detected policy risk");
        response.setModelName("omni-moderation-latest");
        response.setRawCategories(Map.of("violence", true));
        return response;
    }

    private ContentReport blogReport() {
        ContentReport report = baseReport(ReportTargetType.BLOG);
        report.setBlog(blog());
        return report;
    }

    private ContentReport commentReport() {
        ContentReport report = baseReport(ReportTargetType.COMMENT);
        report.setComment(comment());
        return report;
    }

    private ContentReport userReport() {
        ContentReport report = baseReport(ReportTargetType.USER);
        report.setReportedUser(user("reported"));
        return report;
    }

    private ContentReport cafePageReport() {
        ContentReport report = baseReport(ReportTargetType.CAFE_PAGE);
        CafePage cafePage = new CafePage();
        cafePage.setId(UUID.randomUUID());
        report.setCafePage(cafePage);
        return report;
    }

    private ContentReport baseReport(ReportTargetType targetType) {
        ContentReport report = new ContentReport();
        report.setId(UUID.randomUUID());
        report.setReporter(user("reporter"));
        report.setTargetType(targetType);
        report.setReason(reason());
        report.setReasonSnapshot("Spam or violence");
        report.setDescription("This content looks unsafe");
        report.setStatus(ReportStatus.OPEN);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    private Blog blog() {
        Blog blog = new Blog();
        blog.setId(UUID.randomUUID());
        blog.setAuthor(user("author"));
        blog.setContent("Reported blog content");
        blog.setImageUrls(List.of("https://example.com/blog.png"));
        return blog;
    }

    private Comment comment() {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setBlog(blog());
        comment.setUser(user("commenter"));
        comment.setContent("Reported comment content");
        comment.setImageUrls(List.of("https://example.com/comment.png"));
        return comment;
    }

    private ReportReason reason() {
        ReportReason reason = new ReportReason();
        reason.setId(UUID.randomUUID());
        reason.setCode("VIOLENCE_HATE_OR_EXPLOITATION");
        reason.setLabelVi("Violence");
        reason.setSeverity(4);
        return reason;
    }

    private User user(String username) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName(username);
        return user;
    }
}
