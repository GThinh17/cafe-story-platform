package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminCafePageStatusUpdateRequestDTO;
import com.cafestory.dto.responseDTO.CafePageResponseDTO;
import com.cafestory.entity.enums.PageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminCafePageService {

    Page<CafePageResponseDTO> getCafePages(PageStatus status, UUID ownerUserId, Pageable pageable);

    CafePageResponseDTO getCafePage(UUID pageId);

    CafePageResponseDTO updateCafePageStatus(UUID pageId, AdminCafePageStatusUpdateRequestDTO request);

    void deleteCafePage(UUID pageId);
}
