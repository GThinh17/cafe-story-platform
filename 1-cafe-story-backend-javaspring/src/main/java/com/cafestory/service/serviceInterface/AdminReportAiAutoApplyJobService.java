package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiAutoApplyJobResponseDTO;
import com.cafestory.entity.AdminReportAiResolution;
import com.cafestory.entity.ContentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminReportAiAutoApplyJobService {

    ScheduleResult scheduleIfRequested(
            ContentReport report,
            AdminReportAiResolution resolution,
            AdminReportAiResolutionCreateRequestDTO request,
            UUID adminUserId);

    Page<AdminReportAiAutoApplyJobResponseDTO> getJobs(UUID reportId, Pageable pageable);

    AdminReportAiAutoApplyJobResponseDTO cancelJob(UUID jobId, UUID adminUserId);

    int processDueJobs(int limit);

    record ScheduleResult(AdminReportAiAutoApplyJobResponseDTO job, String warning) {
    }
}
