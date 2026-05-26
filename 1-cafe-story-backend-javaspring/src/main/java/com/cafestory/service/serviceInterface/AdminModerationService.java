package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminModerationResolveRequestDTO;
import com.cafestory.dto.responseDTO.AdminModerationResultResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminModerationService {

    Page<AdminModerationResultResponseDTO> getQueue(Pageable pageable);

    AdminModerationResultResponseDTO getResult(UUID resultId);

    AdminModerationResultResponseDTO resolveResult(UUID resultId, AdminModerationResolveRequestDTO request);
}
