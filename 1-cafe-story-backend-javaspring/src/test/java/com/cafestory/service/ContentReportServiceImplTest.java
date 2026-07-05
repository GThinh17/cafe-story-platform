package com.cafestory.service;

import com.cafestory.dto.requestDTO.AdminContentReportStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.ContentReportRequestDTO;
import com.cafestory.dto.responseDTO.ContentReportResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogEvent;
import com.cafestory.entity.Comment;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.ReportReason;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.BlogEventType;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.BlogEventRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.service.serviceImplement.ContentReportServiceImpl;
import com.cafestory.service.serviceInterface.ReportModerationService;
import com.cafestory.service.serviceInterface.ReportReasonService;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.CommentValidator;
import com.cafestory.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentReportServiceImplTest {

    @Mock
    private ContentReportRepository contentReportRepository;

    @Mock
    private BlogEventRepository blogEventRepository;

    @Mock
    private BlogValidator blogValidator;

    @Mock
    private CommentValidator commentValidator;

    @Mock
    private CafePageValidator cafePageValidator;

    @Mock
    private UserValidator userValidator;

    @Mock
    private ReportReasonService reportReasonService;

    @Mock
    private ReportModerationService reportModerationService;

    @InjectMocks
    private ContentReportServiceImpl contentReportService;

    @Test
    void createReport_success_blogReportCreatesRankingEvent_TC001() {
        UUID reporterId = UUID.randomUUID();
        UUID blogId = UUID.randomUUID();
        User reporter = user(reporterId, "reader");
        Blog blog = blog(blogId, user(UUID.randomUUID(), "author"));
        ContentReportRequestDTO request = request(ReportTargetType.BLOG, blogId);
        ReportReason reason = reason(request.getReasonId(), "SCAM_FRAUD_OR_SPAM", "Lừa đảo, gian lận hoặc spam");

        when(userValidator.validateUserExists(reporterId)).thenReturn(reporter);
        when(reportReasonService.validateActiveReportReason(request.getReasonId(), ReportTargetType.BLOG)).thenReturn(reason);
        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(contentReportRepository.existsByReporterUserIdAndBlogIdAndStatusIn(
                reporterId,
                blogId,
                java.util.List.of(ReportStatus.OPEN, ReportStatus.REVIEWING))).thenReturn(false);
        when(contentReportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> saved(invocation.getArgument(0)));

        ContentReportResponseDTO result = contentReportService.createReport(reporterId, request);

        assertThat(result.getReporterUserId()).isEqualTo(reporterId);
        assertThat(result.getTargetType()).isEqualTo(ReportTargetType.BLOG);
        assertThat(result.getTargetId()).isEqualTo(blogId);
        assertThat(result.getBlogId()).isEqualTo(blogId);
        assertThat(result.getReasonId()).isEqualTo(reason.getId());
        assertThat(result.getReasonCode()).isEqualTo("SCAM_FRAUD_OR_SPAM");
        assertThat(result.getReason()).isEqualTo("Lừa đảo, gian lận hoặc spam");
        assertThat(result.getStatus()).isEqualTo(ReportStatus.OPEN);

        ArgumentCaptor<BlogEvent> eventCaptor = ArgumentCaptor.forClass(BlogEvent.class);
        verify(blogEventRepository).save(eventCaptor.capture());
        BlogEvent event = eventCaptor.getValue();
        assertThat(event.getBlog()).isEqualTo(blog);
        assertThat(event.getUser()).isEqualTo(reporter);
        assertThat(event.getEventType()).isEqualTo(BlogEventType.REPORT);
        assertThat(event.getWeight()).isEqualTo(-10.0);
        verify(reportModerationService).enqueueReport(any(ContentReport.class));
    }

    @Test
    void createReport_fail_duplicateOpenBlogReport_TC002() {
        UUID reporterId = UUID.randomUUID();
        UUID blogId = UUID.randomUUID();
        User reporter = user(reporterId, "reader");
        Blog blog = blog(blogId, user(UUID.randomUUID(), "author"));
        ContentReportRequestDTO request = request(ReportTargetType.BLOG, blogId);
        ReportReason reason = reason(request.getReasonId(), "SCAM_FRAUD_OR_SPAM", "Lừa đảo, gian lận hoặc spam");

        when(userValidator.validateUserExists(reporterId)).thenReturn(reporter);
        when(reportReasonService.validateActiveReportReason(request.getReasonId(), ReportTargetType.BLOG)).thenReturn(reason);
        when(blogValidator.validateBlogExists(blogId)).thenReturn(blog);
        when(contentReportRepository.existsByReporterUserIdAndBlogIdAndStatusIn(
                reporterId,
                blogId,
                java.util.List.of(ReportStatus.OPEN, ReportStatus.REVIEWING))).thenReturn(true);

        assertThatThrownBy(() -> contentReportService.createReport(reporterId, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Report already exists for this blog"));

        verify(contentReportRepository, never()).save(any(ContentReport.class));
        verify(blogEventRepository, never()).save(any(BlogEvent.class));
        verifyNoInteractions(reportModerationService);
    }

    @Test
    void createReport_fail_userCannotReportSelf_TC003() {
        UUID userId = UUID.randomUUID();
        User reporter = user(userId, "reader");
        ContentReportRequestDTO request = request(ReportTargetType.USER, userId);
        ReportReason reason = reason(request.getReasonId(), "BULLYING_OR_UNWANTED_CONTACT", "Bắt nạt hoặc liên hệ theo cách không mong muốn");

        when(userValidator.validateUserExists(userId)).thenReturn(reporter);
        when(reportReasonService.validateActiveReportReason(request.getReasonId(), ReportTargetType.USER)).thenReturn(reason);

        assertThatThrownBy(() -> contentReportService.createReport(userId, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Cannot report yourself"));

        verify(contentReportRepository, never()).existsByReporterUserIdAndReportedUserUserIdAndStatusIn(
                any(UUID.class),
                any(UUID.class),
                anyCollection());
        verifyNoInteractions(reportModerationService);
    }

    @Test
    void createReport_fail_requiredDescriptionMissing_TC004() {
        UUID reporterId = UUID.randomUUID();
        UUID blogId = UUID.randomUUID();
        User reporter = user(reporterId, "reader");
        ContentReportRequestDTO request = request(ReportTargetType.BLOG, blogId);
        request.setDescription(null);
        ReportReason reason = reason(request.getReasonId(), "INTELLECTUAL_PROPERTY", "Quyền sở hữu trí tuệ");
        reason.setRequiresDescription(true);

        when(userValidator.validateUserExists(reporterId)).thenReturn(reporter);
        when(reportReasonService.validateActiveReportReason(request.getReasonId(), ReportTargetType.BLOG)).thenReturn(reason);

        assertThatThrownBy(() -> contentReportService.createReport(reporterId, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Report description is required for this reason"));

        verify(contentReportRepository, never()).save(any(ContentReport.class));
        verify(blogEventRepository, never()).save(any(BlogEvent.class));
        verifyNoInteractions(reportModerationService);
    }

    @Test
    void updateStatus_success_resolvedReportGetsResolvedAt_TC005() {
        UUID reportId = UUID.randomUUID();
        ContentReport report = report(reportId, user(UUID.randomUUID(), "reader"));
        AdminContentReportStatusUpdateRequestDTO request = new AdminContentReportStatusUpdateRequestDTO();
        request.setStatus(ReportStatus.RESOLVED);

        when(contentReportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(contentReportRepository.save(report)).thenReturn(report);

        ContentReportResponseDTO result = contentReportService.updateStatus(reportId, request);

        assertThat(result.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(result.getResolvedAt()).isNotNull();
        verify(contentReportRepository).save(report);
    }

    @Test
    void resolveReport_success_openReportGetsResolvedAt_TC006() {
        UUID reportId = UUID.randomUUID();
        ContentReport report = report(reportId, user(UUID.randomUUID(), "reader"));

        when(contentReportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(contentReportRepository.save(report)).thenReturn(report);

        ContentReportResponseDTO result = contentReportService.resolveReport(reportId);

        assertThat(result.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(result.getResolvedAt()).isNotNull();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        verify(contentReportRepository).save(report);
        verifyNoInteractions(reportModerationService);
    }

    @Test
    void resolveReport_success_alreadyResolvedReportIsIdempotent_TC007() {
        UUID reportId = UUID.randomUUID();
        ContentReport report = report(reportId, user(UUID.randomUUID(), "reader"));
        LocalDateTime resolvedAt = LocalDateTime.now().minusDays(1);
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedAt(resolvedAt);

        when(contentReportRepository.findById(reportId)).thenReturn(Optional.of(report));

        ContentReportResponseDTO result = contentReportService.resolveReport(reportId);

        assertThat(result.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(result.getResolvedAt()).isEqualTo(resolvedAt);
        verify(contentReportRepository, never()).save(any(ContentReport.class));
        verifyNoInteractions(reportModerationService);
    }

    @Test
    void resolveReport_fail_reportNotFound_TC008() {
        UUID reportId = UUID.randomUUID();
        when(contentReportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contentReportService.resolveReport(reportId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Report not found"));

        verify(contentReportRepository, never()).save(any(ContentReport.class));
        verifyNoInteractions(reportModerationService);
    }

    @Test
    void resolveReport_fail_nullReportId_TC009() {
        assertThatThrownBy(() -> contentReportService.resolveReport(null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .satisfies(error -> assertThat(((ResponseStatusException) error).getReason())
                        .isEqualTo("Report id is required"));

        verify(contentReportRepository, never()).findById(any(UUID.class));
        verify(contentReportRepository, never()).save(any(ContentReport.class));
        verifyNoInteractions(reportModerationService);
    }

    @Test
    void createReport_success_userReportDoesNotEnqueueModerationJob_TC010() {
        UUID reporterId = UUID.randomUUID();
        UUID reportedUserId = UUID.randomUUID();
        User reporter = user(reporterId, "reader");
        User reportedUser = user(reportedUserId, "reported");
        ContentReportRequestDTO request = request(ReportTargetType.USER, reportedUserId);
        ReportReason reason = reason(request.getReasonId(), "BULLYING_OR_UNWANTED_CONTACT", "Bullying");

        when(userValidator.validateUserExists(reporterId)).thenReturn(reporter);
        when(userValidator.validateUserExists(reportedUserId)).thenReturn(reportedUser);
        when(reportReasonService.validateActiveReportReason(request.getReasonId(), ReportTargetType.USER)).thenReturn(reason);
        when(contentReportRepository.existsByReporterUserIdAndReportedUserUserIdAndStatusIn(
                reporterId,
                reportedUserId,
                java.util.List.of(ReportStatus.OPEN, ReportStatus.REVIEWING))).thenReturn(false);
        when(contentReportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> saved(invocation.getArgument(0)));

        ContentReportResponseDTO result = contentReportService.createReport(reporterId, request);

        assertThat(result.getTargetType()).isEqualTo(ReportTargetType.USER);
        assertThat(result.getReportedUserId()).isEqualTo(reportedUserId);
        verifyNoInteractions(reportModerationService);
        verify(blogEventRepository, never()).save(any(BlogEvent.class));
    }

    @Test
    void createReport_success_commentReportTriggersModeration_TC011() {
        UUID reporterId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        User reporter = user(reporterId, "reader");
        Comment comment = comment(commentId, user(UUID.randomUUID(), "author"));
        ContentReportRequestDTO request = request(ReportTargetType.COMMENT, commentId);
        ReportReason reason = reason(request.getReasonId(), "SCAM_FRAUD_OR_SPAM", "Spam");

        when(userValidator.validateUserExists(reporterId)).thenReturn(reporter);
        when(reportReasonService.validateActiveReportReason(request.getReasonId(), ReportTargetType.COMMENT)).thenReturn(reason);
        when(commentValidator.validateCommentExists(commentId)).thenReturn(comment);
        when(contentReportRepository.existsByReporterUserIdAndCommentIdAndStatusIn(
                reporterId,
                commentId,
                java.util.List.of(ReportStatus.OPEN, ReportStatus.REVIEWING))).thenReturn(false);
        when(contentReportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> saved(invocation.getArgument(0)));

        ContentReportResponseDTO result = contentReportService.createReport(reporterId, request);

        assertThat(result.getTargetType()).isEqualTo(ReportTargetType.COMMENT);
        assertThat(result.getCommentId()).isEqualTo(commentId);
        verify(reportModerationService).enqueueReport(any(ContentReport.class));
        verify(blogEventRepository, never()).save(any(BlogEvent.class));
    }

    private ContentReportRequestDTO request(ReportTargetType targetType, UUID targetId) {
        ContentReportRequestDTO request = new ContentReportRequestDTO();
        request.setTargetType(targetType);
        request.setTargetId(targetId);
        request.setReasonId(UUID.randomUUID());
        request.setDescription("Spam or misleading content");
        return request;
    }

    private ContentReport saved(ContentReport report) {
        report.setId(UUID.randomUUID());
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    private ContentReport report(UUID reportId, User reporter) {
        ContentReport report = new ContentReport();
        report.setId(reportId);
        report.setReporter(reporter);
        report.setTargetType(ReportTargetType.USER);
        report.setReportedUser(user(UUID.randomUUID(), "reported"));
        report.setReason(reason(UUID.randomUUID(), "SCAM_FRAUD_OR_SPAM", "Lừa đảo, gian lận hoặc spam"));
        report.setReasonSnapshot("Lừa đảo, gian lận hoặc spam");
        report.setStatus(ReportStatus.OPEN);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    private ReportReason reason(UUID reasonId, String code, String labelVi) {
        ReportReason reason = new ReportReason();
        reason.setId(reasonId);
        reason.setCode(code);
        reason.setLabelVi(labelVi);
        reason.setSeverity(4);
        reason.setRequiresDescription(false);
        reason.setActive(true);
        reason.setSortOrder(10);
        return reason;
    }

    private Blog blog(UUID blogId, User author) {
        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setAuthor(author);
        blog.setContent("Blog content");
        return blog;
    }

    private Comment comment(UUID commentId, User author) {
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(author);
        comment.setBlog(blog(UUID.randomUUID(), author));
        comment.setContent("Comment content");
        return comment;
    }

    private User user(UUID userId, String username) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName(username);
        user.setUserEmail(username + "@example.com");
        user.setUserPassword("password");
        user.setAccountStatus(true);
        return user;
    }
}
