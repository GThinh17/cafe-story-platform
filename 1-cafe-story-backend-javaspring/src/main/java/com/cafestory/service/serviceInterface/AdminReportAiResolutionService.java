package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminReportAiResolutionCreateRequestDTO;
import com.cafestory.dto.responseDTO.AdminReportAiResolutionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminReportAiResolutionService {

    AdminReportAiResolutionResponseDTO createResolution(UUID reportId);

    AdminReportAiResolutionResponseDTO createResolution(
            UUID reportId,
            AdminReportAiResolutionCreateRequestDTO request,
            UUID adminUserId);

    Page<AdminReportAiResolutionResponseDTO> getResolutions(UUID reportId, Pageable pageable);
}
