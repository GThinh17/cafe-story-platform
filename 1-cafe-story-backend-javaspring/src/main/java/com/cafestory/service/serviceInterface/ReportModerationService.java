package com.cafestory.service.serviceInterface;

import com.cafestory.dto.responseDTO.ReportModerationJobResponseDTO;
import com.cafestory.entity.ContentReport;
import com.cafestory.entity.enums.ReportModerationJobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReportModerationService {

    void enqueueReport(ContentReport report);

    int processDueJobs(int limit);

    ReportModerationJobResponseDTO retryReport(UUID reportId);

    Page<ReportModerationJobResponseDTO> getJobs(ReportModerationJobStatus status, Pageable pageable);
}
