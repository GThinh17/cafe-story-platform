package com.cafestory.controller;

import com.cafestory.dto.requestDTO.AdminContentReportStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.ContentReportResponseDTO;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import com.cafestory.service.serviceInterface.ContentReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminContentReportController {

    private final ContentReportService contentReportService;

    public AdminContentReportController(ContentReportService contentReportService) {
        this.contentReportService = contentReportService;
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

    private Pageable pageable(int page, int size) {
        return PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
