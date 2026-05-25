package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminExtraFeeStatusUpdateRequestDTO;
import com.cafestory.dto.requestDTO.ExtraFeeRequestDTO;
import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminExtraFeeService {

    Page<ExtraFeeResponseDTO> getExtraFees(Boolean status, Pageable pageable);

    ExtraFeeResponseDTO createExtraFee(ExtraFeeRequestDTO request);

    ExtraFeeResponseDTO updateExtraFee(UUID extraFeeId, ExtraFeeRequestDTO request);

    ExtraFeeResponseDTO updateExtraFeeStatus(UUID extraFeeId, AdminExtraFeeStatusUpdateRequestDTO request);

    void deleteExtraFee(UUID extraFeeId);
}
