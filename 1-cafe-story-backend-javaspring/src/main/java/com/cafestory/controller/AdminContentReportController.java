package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminContentReportStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiAutoApplyJobResponseDTO;
import com.cafestory.dto.responseDTO.AdminReportAiResolutionResponseDTO;
import com.cafestory.dto.responseDTO.ContentReportResponseDTO;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.service.serviceInterface.AdminReportAiAutoApplyJobService;
import com.cafestory.service.serviceInterface.AdminReportAiResolutionService;
import com.cafestory.service.serviceInterface.ContentReportService;
import com.cafestory.until.security.AuthenticatedUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.cafestory.until.security.AuthenticationPrincipalUtils.requireUserId;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminContentReportController {

    private final ContentReportService contentReportService;
    private final AdminReportAiResolutionService adminReportAiResolutionService;
    private final AdminReportAiAutoApplyJobService autoApplyJobService;

    public AdminContentReportController(
            ContentReportService contentReportService,
            AdminReportAiResolutionService adminReportAiResolutionService,
            AdminReportAiAutoApplyJobService autoApplyJobService) {
        this.contentReportService = contentReportService;
        this.adminReportAiResolutionService = adminReportAiResolutionService;
        this.autoApplyJobService = autoApplyJobService;
    }

    @GetMapping
    public Page<ContentReportResponseDTO> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportTargetType targetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return contentReportService.getReports(status, targetType, pageable(page, size));
    }

    @GetMapping("/{reportId}")
    public ContentReportResponseDTO getReport(@PathVariable UUID reportId) {
        return contentReportService.getReport(reportId);
    }

    @PatchMapping("/{reportId}/status")
    public ContentReportResponseDTO updateStatus(
            @PathVariable UUID reportId,
            @Valid @RequestBody AdminContentReportStatusUpdateRequestDTO request) {
        return contentReportService.updateStatus(reportId, request);
    }

    @PostMapping("/{reportId}/resolve")
    public ContentReportResponseDTO resolveReport(@PathVariable UUID reportId) {
        return contentReportService.resolveReport(reportId);
    }

    @PostMapping("/{reportId}/ai-resolution")
    public AdminReportAiResolutionResponseDTO createAiResolution(
            @PathVariable UUID reportId,
            @Valid @RequestBody(required = false) AdminReportAiResolutionCreateRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return adminReportAiResolutionService.createResolution(reportId, request, requireUserId(principal));
    }

    @GetMapping("/{reportId}/ai-resolutions")
    public Page<AdminReportAiResolutionResponseDTO> getAiResolutions(
            @PathVariable UUID reportId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminReportAiResolutionService.getResolutions(reportId, pageable(page, size));
    }

    @GetMapping("/{reportId}/ai-auto-resolutions")
    public Page<AdminReportAiAutoApplyJobResponseDTO> getAiAutoResolutions(
            @PathVariable UUID reportId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return autoApplyJobService.getJobs(reportId, pageable(page, size));
    }

    @PostMapping("/ai-auto-resolutions/{jobId}/cancel")
    public AdminReportAiAutoApplyJobResponseDTO cancelAiAutoResolution(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return autoApplyJobService.cancelJob(jobId, requireUserId(principal));
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
