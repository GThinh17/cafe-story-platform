package com.cafestory.service.serviceInterface;

import com.cafestory.dto.requestDTO.AdminPayoutStatusRequest;
import com.cafestory.dto.responseDTO.payout.AdminPayoutResponse;
import com.cafestory.entity.enums.AdminPayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminPayoutService {

    void generateMonthlyPayout(String month);

    Page<AdminPayoutResponse> getPayouts(String month, AdminPayoutStatus status, Pageable pageable);

    AdminPayoutResponse updatePayoutStatus(UUID payoutId, UUID adminUserId, AdminPayoutStatusRequest request);
}
