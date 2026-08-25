package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdFeeRequestDTO;
import com.cafestory.dto.responseDTO.AdFeeResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AdFeeService {

    AdFeeResponseDTO createAdFee(AdFeeRequestDTO request);

    List<AdFeeResponseDTO> getAllAdFees();

    void deleteAdFee(UUID adFeeId);
}
