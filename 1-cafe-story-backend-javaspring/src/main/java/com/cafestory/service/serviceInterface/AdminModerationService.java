package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminModerationResolveRequestDTO;
import com.cafestory.dto.responseDTO.AdminModerationResultResponseDTO;
import com.cafestory.dto.responseDTO.ReportModerationJobResponseDTO;
import com.cafestory.entity.enums.ModerationDecision;
import com.cafestory.entity.enums.ReportModerationJobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminModerationService {

    Page<AdminModerationResultResponseDTO> getAllResults(
            String aiStatus,
            ModerationDecision decision,
            Boolean resolved,
            Pageable pageable);

    Page<AdminModerationResultResponseDTO> getQueue(Pageable pageable);

    AdminModerationResultResponseDTO getResult(UUID resultId);

    AdminModerationResultResponseDTO resolveResult(UUID resultId, AdminModerationResolveRequestDTO request);

    Page<ReportModerationJobResponseDTO> getJobs(ReportModerationJobStatus status, Pageable pageable);

    ReportModerationJobResponseDTO retryReport(UUID reportId);
}
