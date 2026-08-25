package com.cafestory.service.serviceImplement;

import com.cafestory.config.CacheConfig;
import com.cafestory.dto.requestDTO.AdminContentReportStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.ContentReportRequestDTO;
import com.cafestory.dto.responseDTO.ContentReportResponseDTO;
import com.cafestory.entity.Blog;
import com.cafestory.entity.BlogEvent;
import com.cafestory.entity.CafePage;
import com.cafestory.entity.Comment;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.ReportReason;
import com.cafestory.entity.User;
import com.cafestory.entity.enums.BlogEventType;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.repository.BlogEventRepository;
import com.cafestory.repository.ContentReportRepository;
import com.cafestory.service.serviceInterface.ContentReportService;
import com.cafestory.service.serviceInterface.ReportModerationService;
import com.cafestory.service.serviceInterface.ReportReasonService;
import com.cafestory.validation.BlogValidator;
import com.cafestory.validation.CafePageValidator;
import com.cafestory.validation.CommentValidator;
import com.cafestory.validation.UserValidator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ContentReportServiceImpl implements ContentReportService {

    private static final List<ReportStatus> ACTIVE_REPORT_STATUSES =
            List.of(ReportStatus.OPEN, ReportStatus.REVIEWING);

    private final ContentReportRepository contentReportRepository;
    private final BlogEventRepository blogEventRepository;
    private final BlogValidator blogValidator;
    private final CommentValidator commentValidator;
    private final CafePageValidator cafePageValidator;
    private final UserValidator userValidator;
    private final ReportReasonService reportReasonService;
    private final ReportModerationService reportModerationService;

    public ContentReportServiceImpl(
            ContentReportRepository contentReportRepository,
            BlogEventRepository blogEventRepository,
            BlogValidator blogValidator,
            CommentValidator commentValidator,
            CafePageValidator cafePageValidator,
            UserValidator userValidator,
            ReportReasonService reportReasonService,
            ReportModerationService reportModerationService) {
        this.contentReportRepository = contentReportRepository;
        this.blogEventRepository = blogEventRepository;
        this.blogValidator = blogValidator;
        this.commentValidator = commentValidator;
        this.cafePageValidator = cafePageValidator;
        this.userValidator = userValidator;
        this.reportReasonService = reportReasonService;
        this.reportModerationService = reportModerationService;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ORGANIC_FEED_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.PERSONALIZED_FEED_RANKING_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.RECOMMENDATION_CARDS_CACHE, allEntries = true),
            @CacheEvict(cacheNames = CacheConfig.TRENDING_BLOGS_CACHE, allEntries = true)
    })
    public ContentReportResponseDTO createReport(UUID reporterUserId, ContentReportRequestDTO request) {
        User reporter = userValidator.validateUserExists(reporterUserId);
        userValidator.validateUserActive(reporter);
        validateRequest(request);
        ReportReason reason = reportReasonService.validateActiveReportReason(request.getReasonId(), request.getTargetType());
        validateReasonDescription(reason, request.getDescription());

        ContentReport report = new ContentReport();
        report.setReporter(reporter);
        report.setTargetType(request.getTargetType());
        report.setReason(reason);
        report.setReasonSnapshot(reason.getLabelVi());
        report.setDescription(normalizeDescription(request.getDescription()));

        switch (request.getTargetType()) {
            case BLOG -> attachBlogTarget(report, reporterUserId, request.getTargetId());
            case COMMENT -> attachCommentTarget(report, reporterUserId, request.getTargetId());
            case USER -> attachUserTarget(report, reporterUserId, request.getTargetId());
            case CAFE_PAGE -> attachCafePageTarget(report, reporterUserId, request.getTargetId());
        }

        ContentReport savedReport = contentReportRepository.save(report);
        if (savedReport.getTargetType() == ReportTargetType.BLOG) {
            recordBlogReportEvent(savedReport.getBlog(), reporter);
        }
        if (savedReport.getTargetType() == ReportTargetType.BLOG
                || savedReport.getTargetType() == ReportTargetType.COMMENT) {
            reportModerationService.enqueueReport(savedReport);
        }
        return toResponse(savedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentReportResponseDTO> getReports(
            ReportStatus status,
            ReportTargetType targetType,
            Pageable pageable) {
        return contentReportRepository.findAdminReports(status, targetType, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentReportResponseDTO getReport(UUID reportId) {
        return toResponse(findReport(reportId));
    }

    @Override
    @Transactional
    public ContentReportResponseDTO updateStatus(UUID reportId, AdminContentReportStatusUpdateRequestDTO request) {
        if (request == null || request.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report status is required");
        }
        ContentReport report = findReport(reportId);
        report.setStatus(request.getStatus());
        if (request.getStatus() == ReportStatus.RESOLVED || request.getStatus() == ReportStatus.REJECTED) {
            report.setResolvedAt(LocalDateTime.now());
        } else {
            report.setResolvedAt(null);
        }
        return toResponse(contentReportRepository.save(report));
    }

    @Override
    @Transactional
    public ContentReportResponseDTO resolveReport(UUID reportId) {
        ContentReport report = findReport(reportId);
        if (report.getStatus() == ReportStatus.RESOLVED) {
            return toResponse(report);
        }
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedAt(LocalDateTime.now());
        return toResponse(contentReportRepository.save(report));
    }

    private void attachBlogTarget(ContentReport report, UUID reporterUserId, UUID blogId) {
        Blog blog = blogValidator.validateBlogExists(blogId);
        if (blog.getAuthor() != null && reporterUserId.equals(blog.getAuthor().getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot report your own blog");
        }
        if (contentReportRepository.existsByReporterUserIdAndBlogIdAndStatusIn(
                reporterUserId,
                blogId,
                ACTIVE_REPORT_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Report already exists for this blog");
        }
        report.setBlog(blog);
    }

    private void attachCommentTarget(ContentReport report, UUID reporterUserId, UUID commentId) {
        Comment comment = commentValidator.validateCommentExists(commentId);
        if (comment.getUser() != null && reporterUserId.equals(comment.getUser().getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot report your own comment");
        }
        if (contentReportRepository.existsByReporterUserIdAndCommentIdAndStatusIn(
                reporterUserId,
                commentId,
                ACTIVE_REPORT_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Report already exists for this comment");
        }
        report.setComment(comment);
    }

    private void attachUserTarget(ContentReport report, UUID reporterUserId, UUID reportedUserId) {
        User reportedUser = userValidator.validateUserExists(reportedUserId);
        if (reporterUserId.equals(reportedUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot report yourself");
        }
        if (contentReportRepository.existsByReporterUserIdAndReportedUserUserIdAndStatusIn(
                reporterUserId,
                reportedUserId,
                ACTIVE_REPORT_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Report already exists for this user");
        }
        report.setReportedUser(reportedUser);
    }

    private void attachCafePageTarget(ContentReport report, UUID reporterUserId, UUID cafePageId) {
        CafePage cafePage = cafePageValidator.validateCafePageExists(cafePageId);
        if (cafePage.getOwner() != null && reporterUserId.equals(cafePage.getOwner().getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot report your own cafe page");
        }
        if (contentReportRepository.existsByReporterUserIdAndCafePageIdAndStatusIn(
                reporterUserId,
                cafePageId,
                ACTIVE_REPORT_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Report already exists for this cafe page");
        }
        report.setCafePage(cafePage);
    }

    private void recordBlogReportEvent(Blog blog, User reporter) {
        BlogEvent blogEvent = new BlogEvent();
        blogEvent.setBlog(blog);
        blogEvent.setUser(reporter);
        blogEvent.setEventType(BlogEventType.REPORT);
        blogEvent.setWeight(-10.0);
        blogEventRepository.save(blogEvent);
    }

    private ContentReport findReport(UUID reportId) {
        if (reportId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report id is required");
        }
        return contentReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
    }

    private void validateRequest(ContentReportRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report request is required");
        }
        if (request.getTargetType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report target type is required");
        }
        if (request.getTargetId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report target id is required");
        }
        if (request.getReasonId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report reason id is required");
        }
    }

    private void validateReasonDescription(ReportReason reason, String description) {
        if (Boolean.TRUE.equals(reason.getRequiresDescription())
                && (description == null || description.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report description is required for this reason");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private ContentReportResponseDTO toResponse(ContentReport report) {
        ContentReportResponseDTO response = new ContentReportResponseDTO();
        response.setId(report.getId());
        response.setReporterUserId(report.getReporter().getUserId());
        response.setReporterUserName(report.getReporter().getUserName());
        response.setReporterUserAvatar(report.getReporter().getUserAvatar());
        response.setTargetType(report.getTargetType());
        response.setTargetId(targetId(report));
        response.setBlogId(report.getBlog() == null ? null : report.getBlog().getId());
        response.setCommentId(report.getComment() == null ? null : report.getComment().getId());
        response.setReportedUserId(report.getReportedUser() == null ? null : report.getReportedUser().getUserId());
        response.setCafePageId(report.getCafePage() == null ? null : report.getCafePage().getId());
        response.setReasonId(report.getReason() == null ? null : report.getReason().getId());
        response.setReasonCode(report.getReason() == null ? null : report.getReason().getCode());
        response.setReason(report.getReasonSnapshot());
        response.setReasonLabel(report.getReasonSnapshot());
        response.setReasonSeverity(report.getReason() == null ? null : report.getReason().getSeverity());
        response.setDescription(report.getDescription());
        response.setStatus(report.getStatus());
        response.setCreatedAt(report.getCreatedAt());
        response.setResolvedAt(report.getResolvedAt());
        return response;
    }

    private UUID targetId(ContentReport report) {
        return switch (report.getTargetType()) {
            case BLOG -> report.getBlog() == null ? null : report.getBlog().getId();
            case COMMENT -> report.getComment() == null ? null : report.getComment().getId();
            case USER -> report.getReportedUser() == null ? null : report.getReportedUser().getUserId();
            case CAFE_PAGE -> report.getCafePage() == null ? null : report.getCafePage().getId();
        };
    }
}
