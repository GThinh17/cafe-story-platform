package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminContentReportStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.ContentReportRequestDTO;
import com.cafestory.dto.responseDTO.ContentReportResponseDTO;
import com.cafestory.entity.enums.ReportStatus;
import com.cafestory.entity.enums.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ContentReportService {
    ContentReportResponseDTO createReport(UUID reporterUserId, ContentReportRequestDTO request);

    Page<ContentReportResponseDTO> getReports(ReportStatus status, ReportTargetType targetType, Pageable pageable);

    ContentReportResponseDTO getReport(UUID reportId);

    ContentReportResponseDTO updateStatus(UUID reportId, AdminContentReportStatusUpdateRequestDTO request);
}
