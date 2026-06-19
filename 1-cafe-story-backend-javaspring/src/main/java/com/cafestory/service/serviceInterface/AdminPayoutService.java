package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminPayoutStatusRequest;
import com.cafestory.dto.responseDTO.AdminPayoutResponseDTO;
import com.cafestory.entity.enums.AdminPayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminPayoutService {

    void generateMonthlyPayout(String month);

    Page<AdminPayoutResponseDTO> getPayouts(String month, AdminPayoutStatus status, Pageable pageable);

    AdminPayoutResponseDTO updatePayoutStatus(UUID payoutId, UUID adminUserId, AdminPayoutStatusRequest request);
}
