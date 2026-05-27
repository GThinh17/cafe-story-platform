package com.cafestory.service;

import com.cafestory.dto.requestDTO.AdminContentReportStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.ContentReportRequestDTO;
import com.cafestory.dto.responseDTO.ContentReportResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogEvent;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.BlogEventType;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.BlogEventRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.service.serviceImplement.ContentReportServiceImpl;
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

    @InjectMocks
    private ContentReportServiceImpl contentReportService;

    @Test
    void createReport_success_blogReportCreatesRankingEvent_TC001() {
        UUID reporterId = UUID.randomUUID();
        UUID blogId = UUID.randomUUID();
        User reporter = user(reporterId, "reader");
        Blog blog = blog(blogId, user(UUID.randomUUID(), "author"));
        ContentReportRequestDTO request = request(ReportTargetType.BLOG, blogId);

        when(userValidator.validateUserExists(reporterId)).thenReturn(reporter);
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
        assertThat(result.getStatus()).isEqualTo(ReportStatus.OPEN);

        ArgumentCaptor<BlogEvent> eventCaptor = ArgumentCaptor.forClass(BlogEvent.class);
        verify(blogEventRepository).save(eventCaptor.capture());
        BlogEvent event = eventCaptor.getValue();
        assertThat(event.getBlog()).isEqualTo(blog);
        assertThat(event.getUser()).isEqualTo(reporter);
        assertThat(event.getEventType()).isEqualTo(BlogEventType.REPORT);
        assertThat(event.getWeight()).isEqualTo(-10.0);
    }

    @Test
    void createReport_fail_duplicateOpenBlogReport_TC002() {
        UUID reporterId = UUID.randomUUID();
        UUID blogId = UUID.randomUUID();
        User reporter = user(reporterId, "reader");
        Blog blog = blog(blogId, user(UUID.randomUUID(), "author"));
        ContentReportRequestDTO request = request(ReportTargetType.BLOG, blogId);

        when(userValidator.validateUserExists(reporterId)).thenReturn(reporter);
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
    }

    @Test
    void createReport_fail_userCannotReportSelf_TC003() {
        UUID userId = UUID.randomUUID();
        User reporter = user(userId, "reader");
        ContentReportRequestDTO request = request(ReportTargetType.USER, userId);

        when(userValidator.validateUserExists(userId)).thenReturn(reporter);

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
    }

    @Test
    void updateStatus_success_resolvedReportGetsResolvedAt_TC004() {
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

    private ContentReportRequestDTO request(ReportTargetType targetType, UUID targetId) {
        ContentReportRequestDTO request = new ContentReportRequestDTO();
        request.setTargetType(targetType);
        request.setTargetId(targetId);
        request.setReason("SPAM");
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
        report.setReason("SPAM");
        report.setStatus(ReportStatus.OPEN);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    private Blog blog(UUID blogId, User author) {
        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setAuthor(author);
        blog.setContent("Blog content");
        return blog;
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
