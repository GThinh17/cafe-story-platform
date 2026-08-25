package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.ExtraFeeRequestDTO;
import com.cafestory.dto.responseDTO.ExtraFeeResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ExtraFeeService {

    ExtraFeeResponseDTO createExtraFee(ExtraFeeRequestDTO request);

    ExtraFeeResponseDTO updateExtraFee(UUID extraFeeId, ExtraFeeRequestDTO request);

    List<ExtraFeeResponseDTO> getAllExtraFees();

    List<ExtraFeeResponseDTO> getActiveExtraFees();

    void deleteExtraFee(UUID extraFeeId);
}
